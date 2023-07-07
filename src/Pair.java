public class Pair
{
    private int docId, docFrq;
    public Pair(int docId, int docFrq) {
        this.docId = docId;
        this.docFrq = docFrq;
    }

    public int getDocFrq() {
        return docFrq;
    }

    public int getDocId() {
        return docId;
    }
}
