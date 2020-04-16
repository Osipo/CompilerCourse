package ru.osipov.labs.lab1.structures.lists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LinkedList<T> implements Iterable<T>, Collection<T> {
    private ElementType<T> _head;//pointer to the first element. (->) Ignored:: Element field.
    private ElementType<T> _tail;//last element of the list.

    private int _count;

    public LinkedList() {
        this._count = 0;
        this._head = new ElementType<T>();
        this._tail = new ElementType<T>();
        this._tail.setNext(null);
    }

    public int size() {
        return _count;
    }

    //EMPTY(L): BOOLEAN
    public boolean isEmpty() {
        return _count == 0;
    }

    @Override
    public boolean contains(Object o) {
        T t = isT(o);
        if(t != null)
            return Contains(t);
        else
            return false;
    }

    //COPY TO ARRAY(array)
    public void copyTo(T[] array, int arrayIndex) {
        ElementType<T> first = _head.getNext();
        if (arrayIndex < 0 || arrayIndex >= array.length) {
            System.out.println("arrayIndex is out of range");
            return;
        }
        int i = arrayIndex;
        while (first != null && i < array.length) {//THROW ArgumentException IF(array.Length - arrayIndex < Count)
            array[i] = first.getElement();
            first = first.getNext();
            i += 1;
        }
    }
        
    //RETRIEVE.
    public T get(int index){
        ElementType<T> b = _head;
        int q = 0;
        while(b.getNext() != null && q < index){
            q+=1;
            b = b.getNext();
        }
        return b.getElement();
    }


    public T set(int index,T val){
        ElementType<T> b = _head;
        int q = 0;
        while(b.getNext() != null && q < index){
            q+=1;
            b = b.getNext();
        }
        b.setElement(val);
        return val;
    }

    public T remove(int index) {
        if(index >= 0) {
            int q = -1;
            ElementType<T> p = _head;
            while (q < _count && q < index && p != null) {
                p = p.getNext();
                q++;
            }
            removeAt(index + 1);
            return p.getElement();
        }
        else
            return null;
    }



    //0 pointer to the _head. So pos must be [1..count]
    public void add(int p,T item){
        if(p <= 0 || p > _count + 1){
            System.out.println("This position isn't existed in the list");
            return;
        }
        //if(IsEmpty)
        else if(_count == 0){
            this._tail.setElement(item);
            _count+=1;
            this._head.setNext(_tail);
            return;
        }
            
        //Append.
        else if(p == _count + 1){
            _tail.setNext(new ElementType<T>());
            _tail.getNext().setElement(item);
            _tail = _tail.getNext();
            _count+=1;
            return;
        }
        int q = 1;
        ElementType<T> elem = new ElementType<T>();
        elem.setElement(item);
        ElementType<T> temp = _head;//_head pos = 0.
        while(q < p){//move to p.
            temp = temp.getNext();
            q+=1;
        }
        ElementType<T> pp = temp;
        temp = temp.getNext();//temp = p.next
        pp.setNext(elem);//p.next.element = x
        elem.setNext(temp);//p.next.next = temp
        this._count += 1;
    }
        
    //INSERT.
    public void insert(T item, ElementType<T> position){
        ElementType<T> temp = position.getNext();
        ElementType<T> n = new ElementType<T>();
        n.setElement(item);
        n.setNext(temp);
        position.setNext(n);
        this._count +=1;
    }
        
    public void append(T item){
        add(_count+1,item);//to the end.
    }


    public void removeAt(int p){
        int q = 1;
        if(_count == 0)
            return;
        ElementType<T> pp = _head;
        while(q < p && pp.getNext() != null){
            pp = pp.getNext();
            q+=1;
        }
        pp.setNext(pp.getNext().getNext());
        this._count -=1;
    }
        
    //DELETE
    public boolean Remove(T item){
        ElementType<T> p = _head;
        while(p.getNext() != null){
            if(p.getNext().getElement().equals(item)){
                p.setNext(p.getNext().getNext());
                _count -=1;
                return true;
            }
            p = p.getNext();
        }
        return false;
    }
        
    //MAKENULL
    public void clear(){
        this._count = 0;
        this._head.setNext(null);
        this._tail = null;
        this._tail = new ElementType<T>();
        this._tail.setNext(null);
    }
        
    public int indexOf(T item){
        ElementType<T> p = _head.getNext();
        int idx = 1;
        while(p != null){
            if(p.getElement().equals(item)){
                return idx;
            }
            idx+=1;
            p = p.getNext();
        }
        return -1;
    }
        
    public boolean Contains(T item){
        if(indexOf(item) != -1)
            return true;
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }

    @Override
    public Object[] toArray() {
        Object[] ar = new Object[_count];
        ElementType<T> p = _head.getNext();
        int i = 0;
        while(p != null && i < ar.length){
            ar[i] = p.getElement();
            p = p.getNext();
            i++;
        }
        return ar;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        int q = _count;
        append(t);
        return q < _count;
    }

    @Override
    public boolean remove(Object o) {
        T t = isT(o);
        if(t != null)
            return Remove(t);
        else
            return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c){
            if(!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for(Object o : c){
            T t = isT(o);
            if(t != null && add(t))
                continue;
            else
                return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for(Object o : c){
            if(!remove(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    private class Iter implements Iterator<T> {
        ElementType<T> current = _head.getNext();

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            T el = current.getElement();
            current = current.getNext();
            return el;
        }
    }
        

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        ElementType<T> p = _head.getNext();
        while(p != null){
            sb.append(p.getElement().toString()+" ");
            p = p.getNext();
        }
        sb.append("]");
        return sb.toString();
    }

    private T isT(Object obj){
        T is = null;
        try{
            is = (T) obj;
        }
        catch (ClassCastException e){
            return null;
        }
        return is;
    }
}