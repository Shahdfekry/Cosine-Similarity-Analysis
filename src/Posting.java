public class Posting
{
    public Posting next;
    int docId;
    int dtf;

    public Posting(int docId)
    {
        this.dtf = 1;
        this.docId = docId;
        this.next = null;
    }

    public void setNext(int docId)
    {
        next = new Posting(docId);
    }

    public Posting getNext()
    {
        return next;
    }

    public boolean hasNext()
    {
        return next != null;
    }

    public int getDocId()
    {
        return this.docId;
    }

    public int getDtf()
    {
        return this.dtf;
    }

    public void setDtf(int newDtf)
    {
        this.dtf = newDtf;
    }
}
