package simpledb;

import java.io.IOException;

/**
 * The delete operator. Delete reads tuples from its child operator and removes
 * them from the table they belong to.
 */
public class Delete extends Operator {

    private static final long serialVersionUID = 1L;

    private TransactionId txnId;
    private OpIterator child;
    private boolean finished;
    private TupleDesc desc;

    /**
     * Constructor specifying the transaction that this delete belongs to as
     * well as the child to read from.
     *
     * @param t     The transaction this delete runs in
     * @param child The child operator from which to read tuples for deletion
     */
    public Delete(TransactionId t, OpIterator child) {
        this.txnId = t;
        this.child = child;
        this.finished = false;
        this.desc = new TupleDesc(new Type[]{Type.INT_TYPE});
    }

    public TupleDesc getTupleDesc() {
        return desc;
    }

    public void open() throws DbException, TransactionAbortedException {
        finished = false;
        super.open();
    }

    public void close() {
        finished = true;
        super.close();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        close();
        open();
    }

    /**
     * Deletes tuples as they are read from the child operator. Deletes are
     * processed via the buffer pool (which can be accessed via the
     * Database.getBufferPool() method.
     *
     * @return A 1-field tuple containing the number of deleted records.
     * @see Database#getBufferPool
     * @see BufferPool#deleteTuple
     */
    protected Tuple fetchNext() throws TransactionAbortedException, DbException {
        if (finished) {
            return null;
        }

        finished = true;
        int affectRows = 0;
        child.open();
        while (child.hasNext()) {
            Tuple t = child.next();
            try {
                Database.getBufferPool().deleteTuple(txnId, t);
                affectRows++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new Tuple(desc, new Field[]{new IntField(affectRows)});
    }

    @Override
    public OpIterator[] getChildren() {
        return new OpIterator[]{child};
    }

    @Override
    public void setChildren(OpIterator[] children) {
        assert children.length == 1;
        child = children[0];
    }

}
