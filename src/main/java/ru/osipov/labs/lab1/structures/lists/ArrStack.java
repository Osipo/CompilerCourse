package ru.osipov.labs.lab1.structures.lists;

public class ArrStack<T> {
    private int _top;//pointer to the top of the stack.
    private int _count;//count
    private int _maxlength;//capacity.
    private T[] _base;

    public ArrStack(T[] array){
        this._base = array;
        this._maxlength = array.length;
        this._count = array.length;
        this._top = 0;
    }

    public ArrStack(int capacity){
        this._base = (T[]) new Object[capacity];
        this._maxlength = capacity;
        this._count = 0;
        this._top = capacity;//empty.
    }

    //EMPTY(S): BOOLEAN
    public Boolean isEmpty(){
        return (this._top == _maxlength); //top > lastIndexOfArray
    }

    //TOP
    public T top(){
        if(isEmpty()){
            System.out.println("Error. Stack is empty");
            return null;
        }
        else return _base[_top];
    }

    //POP
    public void pop(){
        if(isEmpty()){
            System.out.println("Error. Stack is empty");
            return;
        }
        else{
            _base[_top] = null;
                this._count -= 1;
                this._top += 1;//move to the end...
        }
    }

    //PUSH
    public void push(T item){
        if(_top == 0){
            System.out.println("Error. Stack is filled");
            return;
        }
        else{
            this._top -= 1;//move to the begining...
            this._count += 1;
            _base[_top] = item;
        }
    }
    public void add(T item){
        push(item);
    }

    public boolean remove(T item){
        if(isEmpty()){
            System.out.println("Error. Stack is empty");
            return false;
        }
        else if(_base[_top].equals(item)){
            _base[_top] = null;
            _top += 1;
            _count -= 1;
            return true;
        }
        else
            return false;
    }

    public boolean Contains(T item){
        int q = _top;
        while(q < _maxlength){
            if(_base[q].equals(item)){
                return true;
            }
            q+=1;
        }
        return false;
    }

    //MAKENULL
    public void clear(){
        this._base = null;
        this._base = (T[]) new Object[_maxlength];
        this._count = 0;
        this._top  = _maxlength;//empty.
    }
}
