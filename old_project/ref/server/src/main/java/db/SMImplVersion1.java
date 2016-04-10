package db;

import java.util.*;
import java.io.*;

/**
 *  Description of the Class
 *
 *@author     Paul Nguyen
 *@created    March 18, 2003
 */
public class SMImplVersion1 implements SM {
  /**
   *  Constructor for the SMImplVersion1 object
   */
  public SMImplVersion1() { }


  public SM.OID getOID( byte[] oidbytes ) {
	return null ;
  }


  /**
   *  Description of the Method
   *
   *@param  rec              Description of Parameter
   *@return                  Description of the Returned Value
   *@exception  IOException  Description of Exception
   */
  public SM.OID store(Record rec) throws IOException {
    OID oid = new OID(rec.hashCode());
    this.buffer.put(oid.getKey(), rec);
    return oid;
  }


  /**
   *  Description of the Method
   *
   *@param  oid                    Description of Parameter
   *@return                        Description of the Returned Value
   *@exception  NotFoundException  Description of Exception
   *@exception  IOException        Description of Exception
   */
  public Record fetch(SM.OID oid) throws NotFoundException, IOException {
    Object rec = null;
    rec = this.buffer.get(oid.getKey());
    if (rec == null) {
      throw new NotFoundException();
    } else {
      return (Record) rec;
    }
  }

    public void close () throws SM.IOException
    {

    }

    public void flush () 
    {

    }


  /**
   *  Description of the Method
   *
   *@param  oid                    Description of Parameter
   *@param  rec                    Description of Parameter
   *@return                        Description of the Returned Value
   *@exception  NotFoundException  Description of Exception
   *@exception  IOException        Description of Exception
   */
  public SM.OID update(SM.OID oid, Record rec) throws NotFoundException, IOException {
    this.buffer.remove(oid.getKey());
    OID newkey = new OID(rec.hashCode());
    this.buffer.put(newkey.getKey(), rec);
    return newkey;
  }


  /**
   *  Description of the Method
   *
   *@param  oid                        Description of Parameter
   *@exception  NotFoundException      Description of Exception
   *@exception  CannotDeleteException  Description of Exception
   */
  public void delete(SM.OID oid) throws NotFoundException, CannotDeleteException {
    this.buffer.remove(oid.getKey());
  }


  /**
   *  Description of the Class
   *
   *@author     Paul Nguyen
   *@created    March 18, 2003
   */
  public class OID implements SM.OID {
    private int key;


    /**
     *  Constructor for the OID object
     *
     *@param  key  Description of Parameter
     */
    public OID(int key) {
      this.key = key;
    }

   public byte[] toBytes()
   {
       return getKey().getBytes();
   }

    /**
     *  Gets the key attribute of the OID object
     *
     *@return    The key value
     */
    public String getKey() {
      return Integer.toString(this.key);
    }
  }


  private Hashtable buffer = new Hashtable();
}

