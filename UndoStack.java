public class UndoStack {
        private Node top;


        //Push
        public void push(String action, Object data){
                Node newNode = new Node(action, data);
                newNode.setNext(top);
                top = newNode;
        }

        //Pop
        public Node pop(){
                if(top == null) return null;
                Node temp = top;
                top = top.getNext();
                return temp;                
        }

        //Check if stack empty
        public boolean isEmpty(){
                if(top == null) return true;
                return false;
        }
}