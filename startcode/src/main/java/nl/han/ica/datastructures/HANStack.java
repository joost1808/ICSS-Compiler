package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {

    private final HANLinkedList<T> list;

    public HANStack() {
        list = new HANLinkedList<>();
    }

    @Override
    public void push(T value) {
        list.addFirst(value);
    }

    @Override
    public T pop() {
        T value = list.getFirst();
        list.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        return list.getFirst();
    }
}
