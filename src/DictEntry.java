import java.util.ArrayList;

public class DictEntry
{
    int docFreq = 0; //number of documents that contain the term
    int termFreq = 0; //number of times the term is mentioned in the collection
    Posting head = null;
    public DictEntry()
    {
        this.docFreq = 0;
        this.termFreq = 0;
    }

    public void add(int docId)
    {
        this.termFreq++;
        if(head == null) {
            head = new Posting(docId);
            docFreq++;
        }else {
            Posting curr = head, prv = null;
            while(curr != null && curr.getDocId() != docId) {
                prv = curr;
                curr = curr.getNext();
            }
            if(curr == null) {
                docFreq++;
                prv.setNext(docId);
            }else {
                curr.setDtf(curr.getDtf()+1);
            }
        }
    }


    public ArrayList<Pair> getDocsIdAndFrq()
    {
        ArrayList<Pair> result = new ArrayList<>();
        Posting curr = head;
        while(curr != null)
        {
            result.add(new Pair(curr.getDocId(), curr.getDtf()));
            curr = curr.getNext();
        }
        return result;
    }


    public int getTermFrequency() {
        return termFreq;
    }

    public int getDocumentFrequency() {
        return docFreq;
    }
}
