package com.xml.library.utils;

import java.util.Random;

/**
 * Created by xlc on 2016/12/30.
 */
public class RUtil {


    public static int get_proportion(int[] weightArrays) {

        int tatol = getTalo(weightArrays);

        int radnum = new Random().nextInt(tatol);

        int l=weightArrays[0];

        int m=weightArrays[0]+weightArrays[1];

        if (radnum < l) {

            return 0;

        } else if (radnum >= l && radnum< m) {

            return 1;
        } else {

            return 2;
        }
    }
    private static int getTalo(int[] weightArrays) {
        int t = 0;

        for (int i = 0; i < weightArrays.length; i++) {

            t = t + weightArrays[i];
        }
        return t;
    }

}
