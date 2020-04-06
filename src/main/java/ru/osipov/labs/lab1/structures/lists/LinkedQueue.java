package ru.osipov.labs.lab1.structures.lists;

import java.util.Collection;
import java.util.Iterator;

//FIFO
public class LinkedQueue<T> implements Collection<T>, Iterable<T> {

    protected ElementType<T> _front;//pointer to the first element. (->) Ignored:: Element field.
    protected ElementType<T> _rear;//pointer to the last element. (->)
    protected int _count;

    public LinkedQueue(){
        this._count = 0;
        this._front = new ElementType<T>();
        this._front.setNext(null);
        this._rear = _front;
    }

    @Override
    public int size() {
        return _count;
    }

    @Override
    public boolean isEmpty() {
        return this._front == this._rear;
    }

    //FRONT(Q)
    public T front(){
        if(isEmpty()){
            System.out.println("Error. Queue is empty");
            return null;
        }
        else
            return this._front.getNext().getElement();
    }

    //ENQUEUE(Q)
    public void enqueue(T item){
        this._rear.setNext(new ElementType<T>());//new(rear.next)
        this._rear = this._rear.getNext();//rear = rear.next
        this._rear.setElement(item);//rear.element = x
        this._rear.setNext(null);//rear.next = nil
        this._count +=1;
    }

    //DEQUEUE(Q)
    public void dequeue(){
        if(isEmpty()){
            System.out.println("Error. Queue is empty");
            return;
        }
        else{
            this._front = this._front.getNext(); //Q.front = Q.front.next.
            this._count -= 1;
        }
    }

    //CONTAINS(Q, element)
    @Override
    public boolean contains(Object o) {
        T item;
        try{
            item = (T) o;
        }catch (ClassCastException  NullPointerException){
            return false;
        }

        ElementType<T> f = this._front.getNext();
        while(f != null){
            if(f.getElement().equals(item)){
                return true;
            }
            f = f.getNext();
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new QueueIterator<>(this);
    }

    @Override
    public Object[] toArray() {
        Object[] a = new Object[_count];
        ElementType<T> f = _front.getNext();
        for(int i = 0; i < a.length && f != null; i++){
            a[i] = f.getElement();
            f = f.getNext();
        }
        return a;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        ElementType<T> f = _front.getNext();
        for(int i = 0; i < a.length && f != null; i++){
            a[i] = (T1) f.getElement();
            f = f.getNext();
        }
        return a;
    }

    @Override
    public boolean add(T t) {
        int c = _count;
        enqueue(t);
        return _count != c;
    }

    @Override
    public boolean remove(Object o) {
        if(isEmpty()){
            System.out.println("Error. Queue is empty");
            return false;
        }

        T item;
        try{
            item = (T) o;
        }catch (ClassCastException  NullPointerException){
            return false;
        }

        if(this._front.getNext().getElement().equals(item)){
            this._front = this._front.getNext(); //Q.front = Q.front.next.
            this._count -= 1;
            return true;
        }
        else
            return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean f = false;
        for(Object o : c){
            f = contains(o);
        }
        return f;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        int ns = _count + c.size();
        for(T item : c){
            enqueue(item);
        }
        return _count == ns;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int ns = _count - c.size();
        for(Object item : c){
            remove(item);
        }
        return _count == ns;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean f = false;
        for(Object item : c){
            if(!contains(item))
                f = remove(item);
        }
        return f;
    }

    //MAKE_EMPTY(Q)
    @Override
    public void clear() {
        this._front = new ElementType<T>();
        this._front.setNext(null);
        this._rear = _front;
        this._count = 0;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("^[ ");
        ElementType<T> p = _front.getNext();
        while(p != null){
            sb.append(p.getElement().toString()+" ");
            p = p.getNext();
        }
        sb.append("]$");
        return sb.toString();
    }

    private class QueueIterator<T> implements Iterator<T> {

        private ElementType<T> c;
        QueueIterator(LinkedQueue<T> q){
            c = q._front;
        }

        @Override
        public boolean hasNext() {
            return c.getNext() != null;
        }

        @Override
        public T next() {
            c = c.getNext();
            return c.getElement();
        }
    }
}