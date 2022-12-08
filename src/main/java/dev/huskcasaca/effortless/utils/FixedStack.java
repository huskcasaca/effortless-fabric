package dev.huskcasaca.effortless.utils;

//Stack with fixed size. Removes (overwrites) oldest element on push.
public class FixedStack<T> {
    private final T[] stack;
    private final int size;
    private int top;
    private int filled = 0; //how many valid items are in the stack

    public FixedStack(T[] stack) {
        this.stack = stack;
        this.top = 0;
        this.size = stack.length;
    }

    public void push(T object) {
        if (top == stack.length)
            top = 0;

        stack[top] = object;
        top++;

        if (filled < size)
            filled++;
    }

    public T pop() {
        if (filled <= 0) return null;

        if (top - 1 < 0)
            top = size;

        top--;
        T object = stack[top];
        filled--;

        return object;
    }

    public void clear() {
        top = 0;
        filled = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return filled <= 0;
    }
}
