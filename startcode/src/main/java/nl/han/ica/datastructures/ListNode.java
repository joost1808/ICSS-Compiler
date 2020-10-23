package nl.han.ica.datastructures;

public class ListNode<T> {
    public T element;
    public ListNode<T> next;

    public ListNode(T theElement) {
        this(theElement, null);
    }

    public ListNode(T theElement, ListNode<T> n) {
        element = theElement;
        next = n;
    }
}
