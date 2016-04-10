package db;

public interface RecordSetIterator {
    void   reset() ;
    byte[] getNextRecord();
    boolean hasMoreRecords() ;
}
