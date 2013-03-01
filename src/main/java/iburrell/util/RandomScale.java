// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RandomScale.java

package iburrell.util;

import java.util.Random;

public class RandomScale
{

    public RandomScale(long l)
    {
        gen = new Random(l);
    }

    public RandomScale()
    {
        gen = new Random();
    }

    public double randomDouble()
    {
        return gen.nextDouble();
    }

    public double randomDouble(double d)
    {
        return gen.nextDouble() * d;
    }

    public double randomDouble(double d, double d1)
    {
        return randomDouble(d1 - d) + d;
    }

    public int randomInt()
    {
        return Math.abs(gen.nextInt());
    }

    public int randomInt(int i)
    {
        return (int)Math.floor(randomDouble(i));
    }

    public int randomInt(int i, int j)
    {
        return randomInt(j - i) + i;
    }

    private Random gen;
    private final int RAND_MAX = 0x7fffffff;
}
