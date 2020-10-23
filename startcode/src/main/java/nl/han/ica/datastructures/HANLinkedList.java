package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private final ListNode<T> topOfStack = new ListNode<>(null);
    private int size = 0;

    @Override
    public void addFirst(T value) {
        ListNode<T> tmp = new ListNode<>(value);
        tmp.next = topOfStack.next;
        topOfStack.next = tmp;
        size++;
    }

    @Override
    public void clear() {
        topOfStack.next = null;
        size = 0;
    }

    @Override
    public void insert(int index, T value) {
        ListNode<T> toBeAdded = new ListNode<>(value);
        ListNode<T> previous = getPrevious(index);
        ListNode<T> toAppend = previous.next;
        previous.next = toBeAdded;
        toBeAdded.next = toAppend;
        size++;
    }

    @Override
    public void delete(int pos) {
        ListNode<T> previous = getPrevious(pos);
        if (previous.next == null) throw new IndexOutOfBoundsException(pos);
        previous.next = previous.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        ListNode<T> previous = getPrevious(pos);
        if (previous.next == null) throw new IndexOutOfBoundsException(pos);
        return previous.next.element;
    }

    private ListNode<T> getPrevious(int index) {
        ListNode<T> previous = topOfStack;
        for (int i = 0; i < index; i++) {
            if (previous.next == null) throw new IndexOutOfBoundsException(index);
            previous = previous.next;
        }
        return previous;
    }

    @Override
    public void removeFirst() {
        delete(0);
    }

    @Override
    public T getFirst() {
//        return get(0);
        if (topOfStack.next != null) {
            return topOfStack.next.element;
        } else {
            return null;
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Iterator iterator() {
        return null;
    }
}
