public class Node{
        private String action;
        private Object data;
        private Node next;

        public Node(String action, Object data){
                this.action = action;
                this.data = data;
                this.next = null;
        }

        public Node(String action, Object data, Node next){
                this.action = action;
                this.data = data;
                this.next = next;
        }

        //getter
        public String getAction(){ return action; }
        public Object getData(){ return data; }
        public Node getNext(){ return next; }

        //setter for next
        public void setNext(Node next){ this.next = next; }

}