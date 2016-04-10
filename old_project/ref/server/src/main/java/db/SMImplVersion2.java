package db;

import java.util.*;
import java.io.*;
import java.nio.*;

/**
 *  Description of the Class
 *
 *@author     Paul Nguyen
 *@created    March 18, 2003
 */
public class SMImplVersion2 implements SM {
  
  private final static boolean DEBUG = true ;
  private final static int SMALL_REC_SIZE = Device.BLOCK_SIZE ;
  private Hashtable buffer = new Hashtable(); 
  
  private Volume master_volume = null ;   // volume id = 0
  private Volume data_volume = null ;     // volume id = 1 
  private Volume index_volume = null ;    // volume id = 2
  private ByteBuffer translation_table = null ; // guid to recid translation table
  private Hashtable lookupcache = new Hashtable() ; // useful only for translation table lookups, not durable
  
  
  private void trans_table_reset() {
    this.translation_table.clear() ;
    ByteBuffer tbl = this.translation_table.duplicate() ;
    tbl.clear() ;
    byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
    byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
    while( tbl.position() < (tbl.limit()-Volume.RECORD_GUID_SIZE+Volume.RECORD_ID_SIZE) ) {
       tbl.get( guid, 0, Volume.RECORD_GUID_SIZE ) ;
       tbl.get( recid, 0, Volume.RECORD_ID_SIZE ) ;
       if ( ! ("00.00.00.00.00.00.00.00".equals(Util.toHexString(recid))) ) {
           int newpos = this.translation_table.position() + Volume.RECORD_GUID_SIZE+Volume.RECORD_ID_SIZE ;
           this.translation_table.position( newpos ) ;
       }
    }        
  }

  private byte[] trans_table_find_entry( byte[] find_guid ) {
    
    
    // try to avoid disk i/o
    byte[] cachehit = (byte[]) lookupcache.get( find_guid ) ;
    if ( cachehit != null )
        return cachehit ;
    
    ByteBuffer tbl = this.translation_table.duplicate() ;
    tbl.clear() ;
    byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
    byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
    String findstr = Util.toHexString( find_guid ) ;
    while( tbl.position() < this.translation_table.position() ) {
       tbl.get( guid, 0, Volume.RECORD_GUID_SIZE ) ;
       tbl.get( recid, 0, Volume.RECORD_ID_SIZE ) ;
       if (DEBUG) {
          System.out.println( "Looking for: " + findstr ) ;
          System.out.println( "Entry Value: " + Util.toHexString(guid) ) ;
       }
       if ( findstr.equals(Util.toHexString(guid)) ) {
          return recid ;
       }
    }  
    if (DEBUG)
      System.out.println( "Entry Not Found!  Return NULL" ) ;
    return null ;
  }
  
  private void trans_table_add_entry( byte[] guid, byte[] recid ) {
      this.translation_table.put( guid ) ;
      this.translation_table.put( recid ) ;  
      lookupcache.put( guid, recid ) ;
  }
  
  private void trans_table_update_entry( byte[] find_guid, byte[] new_recid ) {
    ByteBuffer tbl = this.translation_table.duplicate() ;
    tbl.clear() ;
    byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
    byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
    String findstr = Util.toHexString( find_guid ) ;
    while( tbl.position() < this.translation_table.position() ) {
       tbl.get( guid, 0, Volume.RECORD_GUID_SIZE ) ;
       tbl.get( recid, 0, Volume.RECORD_ID_SIZE ) ;
       if ( findstr.equals(Util.toHexString(guid)) ) {
          int newpos = tbl.position() - Volume.RECORD_ID_SIZE ;
          tbl.position( newpos ) ;
          tbl.put( new_recid ) ;
          lookupcache.put( guid, new_recid ) ;
          return ;
       }
    }        
  }
  
  private void trans_table_delete_entry( byte[] find_guid ) {
    ByteBuffer tbl = this.translation_table.duplicate() ;
    tbl.clear() ;
    byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
    byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
    String findstr = Util.toHexString( find_guid ) ;
    while( tbl.position() < this.translation_table.position() ) {
       tbl.get( guid, 0, Volume.RECORD_GUID_SIZE ) ;
       tbl.get( recid, 0, Volume.RECORD_ID_SIZE ) ;
       if ( findstr.equals(Util.toHexString(guid)) ) {
          int newpos = tbl.position() - (Volume.RECORD_GUID_SIZE+Volume.RECORD_ID_SIZE) ;
          tbl.position( newpos ) ;
          byte zero = 0 ;
          Arrays.fill( guid, zero ) ;
          Arrays.fill( recid, zero ) ;          
          tbl.put( guid ) ;
          tbl.put( recid ) ;
          lookupcache.remove( find_guid ) ;
          return ;
       }
    }            
  }
  
  private void trans_table_dump() {
    System.out.println( "-- Translation Table --" ) ;
    System.out.println( this.translation_table ) ;
    ByteBuffer tbl = this.translation_table.duplicate() ;
    tbl.clear() ;
    byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
    byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
    while( tbl.position() < (tbl.limit()-Volume.RECORD_GUID_SIZE+Volume.RECORD_ID_SIZE) ) {
       tbl.get( guid, 0, Volume.RECORD_GUID_SIZE ) ;
       tbl.get( recid, 0, Volume.RECORD_ID_SIZE ) ;
       if ( ! ("00.00.00.00.00.00.00.00".equals(Util.toHexString(recid))) ) {
        System.out.println( "GUID[" + Util.toHexString(guid) + "] => RECID[" + Util.toHexString(recid) + "] " ) ; 
       }
    }    
    System.out.println("-- Translation Table --");
  }
  

  
  /**
   *  Constructor for the SMImplVersion2 object
   *
   *@since
   */
  public SMImplVersion2() {
    
    // load GUID Pool
    Util.generateGUID() ;

    // mount the bootstrap volume
    master_volume = new Volume(0,"master.db",2,10); // only need two pages, max of 10 pages
    data_volume = new Volume(1,"data.db",50,200);
    index_volume = new Volume(2,"index.db",10,50);

    // create translation table if needed
    Page m_page = master_volume.getPage(0) ;
    ByteBuffer rec_id = ByteBuffer.allocate(8);
    rec_id.putInt(0);
    rec_id.putShort((short) 0);
    rec_id.putShort((short) 1);
    byte[] rec_id_bytes = rec_id.array();    
    ByteBuffer trans_table = m_page.getRecordByteBuffer( rec_id_bytes ) ;
    if ( trans_table == null ) {
      if (DEBUG) {
        System.out.println( "Creating Translation Table..." ) ; 
      }
      byte[] trans_table_array = new byte[ Volume.MAX_TRANSLATION_TABLE_SIZE_PER_PAGE ] ;
      try { 
        byte[] trans_table_recid = m_page.addRecord( trans_table_array ) ;
      } catch (Exception e) {}
      trans_table = m_page.getRecordByteBuffer( rec_id_bytes ) ;
    }
    this.translation_table = trans_table ;
    
    // advance buffer position to end to be ready for new inserts
    trans_table_reset() ;
    
    /* test create a sample trans table entry */    
    if (false) {
      trans_table_dump() ;        
      byte[] guid = new byte[ Volume.RECORD_GUID_SIZE ] ;
      byte[] recid = new byte[ Volume.RECORD_ID_SIZE ] ;
      byte b = -1 ;
      Arrays.fill( guid, b ) ;
      Arrays.fill( recid, b ) ;
      this.translation_table.put( guid ) ;
      this.translation_table.put( recid ) ;
      trans_table_dump() ;    
      byte[] found = trans_table_find_entry( guid ) ;
      System.out.println( "FOUND => " + Util.toHexString(found) ) ;
      b = 99 ;
      Arrays.fill( recid, b ) ;
      trans_table_update_entry( guid, recid ) ;
      trans_table_dump() ;
      trans_table_delete_entry( guid ) ;
      trans_table_dump() ;
    }
  }


  /**
   *  Description of the Method
   *
   *@param  rec              Description of Parameter
   *@return                  Description of the Returned Value
   *@exception  IOException  Description of Exception
   *@since
   */
  public SM.OID store(Record rec) throws IOException {  

	  Record therec = null ;
	  
	  try {
	  if (rec == null) {
		  therec = new Record(0) ;
	  }
	  else {
		  therec = rec ;
	  }
	  } catch ( Exception e ) {
		  throw new IOException() ;
	  }

	  //System.out.println( "REC LEN: " + rec.length() ) ;

	  if ( therec.length() < SMALL_REC_SIZE ) {
		  OID oid = new OID(therec.hashCode());
		  this.buffer.put(oid.getKey(), therec);
		  return oid;		  
	  }
	
 
    Page freepage = null ;
    byte[] recid = null ;
    try {
      freepage = data_volume.getFreePage() ;
      recid = freepage.addRecord( therec.getBytes(0,0) ) ;
    } catch (PageFullException e) {
      if (DEBUG) {
        System.out.println( "Page Full, allocating a new page..." ) ;
      }
      try {
	   freepage = data_volume.newFreePage() ;
        recid = freepage.addRecord( therec.getBytes(0,0) ) ;
      } catch (Exception e2) {
		 if (DEBUG) {
			 System.out.println( "Volume Full Error! -- " + e2.getMessage() ) ;
			 e2.printStackTrace() ;
		 }
		 throw new IOException() ;
	 }
    }
    byte[] guid = Util.generateGUID() ;
    trans_table_add_entry( guid, recid ) ;
    
    if (DEBUG && guid != null)
      System.out.println( "Stored With OID: " + Util.toHexString(guid) ) ;

    OID ret = new OID(guid) ;

    // remove null record
    /*
    if (rec==null) {
    if (DEBUG)
		    System.out.println( "Remove NULL Record with OID: " + Util.toHexString(guid) ) ;
	    trans_table_delete_entry( guid ) ;
	    try { delete( ret ) ; } catch (Exception e) {} 
    }
    */

    return ret ;   
  }


  /**
   *  Description of the Method
   *
   *@param  oid                    Description of Parameter
   *@return                        Description of the Returned Value
   *@exception  NotFoundException  Description of Exception
   *@exception  IOException        Description of Exception
   *@since
   */
  public Record fetch(SM.OID oid) throws NotFoundException, IOException {

    Object smallrec = null;
    smallrec = this.buffer.get(oid.getKey());
    if (smallrec != null) {
	 return (Record) smallrec;
    }


    //if (DEBUG)
    //  System.out.println( "Fetch Using OID: " + Util.toHexString(oid.toBytes()) ) ;
    
    Record rec = null ;    
    try {
      
      byte[] recid = trans_table_find_entry( oid.toBytes() ) ;
      if ( recid == null )
        throw new NotFoundException() ;      

      if (DEBUG & recid != null)
        System.out.println( "Fetch Found RECID: " + Util.toHexString(recid) ) ;
        
      ByteBuffer recid_buf = ByteBuffer.wrap(recid);
      int rec_vol_id = recid_buf.getInt() ;
      int rec_page_id = (int)recid_buf.getShort();
      Page datapage = data_volume.getPage( rec_page_id ) ;
      byte[] data = datapage.getRecord( recid ) ;
	 if ( data == null )
		 throw new NotFoundException();
	 if (DEBUG && data != null)
		 System.out.println( "Allocating new record with " + data.length ) ;
      rec = new Record( data.length ) ;
      rec.setBytes( data ) ;    
      return rec ;
    } catch ( Exception e ) {
	    //System.out.println("fetch() Error: " + e);
      throw new NotFoundException();
    }

  }


  /**
   *  Description of the Method
   *
   *@exception  SM.IOException  Description of Exception
   *@since
   */
  public void close() throws SM.IOException { 
    master_volume.close() ;
    data_volume.close() ;
    index_volume.close() ;
  }

  public void flush() {
	  try {
		  close() ;
	  }
	  catch ( Exception e ) {}
  }

  /**
   *  Description of the Method
   *
   *@param  oid                    Description of Parameter
   *@param  rec                    Description of Parameter
   *@return                        Description of the Returned Value
   *@exception  NotFoundException  Description of Exception
   *@exception  IOException        Description of Exception
   *@since
   */
  public SM.OID update(SM.OID oid, Record rec) throws NotFoundException, IOException {

	  if (rec == null)
		  throw new IOException() ;


    Object smallrec = null;
    smallrec = this.buffer.get(oid.getKey());
    if (smallrec != null) {
	  this.buffer.remove(oid.getKey());
	  this.buffer.put(oid.getKey(), rec);
	  return oid;
    }
	  


      //if (DEBUG)
      //System.out.println( "Update Using OID: " + Util.toHexString(oid.toBytes()) ) ;

    try {
      byte[] recid = trans_table_find_entry( oid.toBytes() ) ;
      if ( recid == null )
        throw new NotFoundException() ;      
      ByteBuffer recid_buf = ByteBuffer.wrap(recid);
      int rec_vol_id = recid_buf.getInt() ;
      int rec_page_id = (int)recid_buf.getShort();
      Page datapage = data_volume.getPage( rec_page_id ) ;
      byte[] new_rec_id = datapage.updateRecord( recid, rec.getBytes(0,0) ) ;
      trans_table_update_entry( oid.toBytes(), new_rec_id ) ;
      return oid ;
    } catch ( Exception e ) {
      throw new NotFoundException();
    }
    
  }


  /**
   *  Description of the Method
   *
   *@param  oid                        Description of Parameter
   *@exception  NotFoundException      Description of Exception
   *@exception  CannotDeleteException  Description of Exception
   *@since
   */
  public void delete(SM.OID oid) throws NotFoundException, CannotDeleteException {

    Object rec = null;
    rec = this.buffer.get(oid.getKey());
    if (rec != null) {
	  this.buffer.remove(oid.getKey());
	  return ;
    }
	  

    //if (DEBUG && oid != null)
    //  System.out.println( "Delete Using OID: " + Util.toHexString(oid.toBytes()) ) ;
  
    try {
      byte[] recid = trans_table_find_entry( oid.toBytes() ) ;
      if ( recid == null )
        throw new NotFoundException() ;            
      ByteBuffer recid_buf = ByteBuffer.wrap(recid);
      int rec_vol_id = recid_buf.getInt() ;
      int rec_page_id = (int)recid_buf.getShort();
      Page datapage = data_volume.getPage( rec_page_id ) ;
      datapage.deleteRecord( recid ) ;
      trans_table_delete_entry( oid.toBytes() ) ;
    } catch ( Exception e ) {
      throw new NotFoundException();
    }

  }


  public SM.OID getOID( byte[] oidbytes ) {
	return new SMImplVersion2.OID( oidbytes ) ;	  
  }

  /**
   *  Description of the Class
   *
   *@author     Paul Nguyen
   *@created    March 18, 2003
   */
  public class OID implements SM.OID {
    private byte[] key;
    private int intKey;
    private int keyType = 0;

    /**
     *  Constructor for the OID object
     *
     *@param  key  Description of Parameter
     *@since
     */
    public OID(byte[] key) {
      this.key = key;
    }

    public OID(int key) {
	    this.key = Util.generateGUID() ;
    }


    /**
     *  Gets the key attribute of the OID object
     *
     *@return    The key value
     *@since
     */
    public String getKey() {
		return Util.toHexString(this.key) ;
    }


    /**
     *  Description of the Method
     *
     *@return    Description of the Returned Value
     *@since
     */
    public byte[] toBytes() {

	    return this.key ;

    }
  }
}

