package ru.osipov.labs.lab2.jsonParser;

//Process octal,hex,binary,decimal scientific literals to decimal number.
public class ProcessExp {
    public static double parse(String num, String e, char c,int base,int sign){//c - type of exponent.
        double me = 1;
        if(c == 'P' || c == 'p')//binary exponent.
            me = Math.pow(2.0,Double.parseDouble(e));
        else if(c == 'E' || c == 'e' || c == 'H' || c == 'h')//H and h are exponent for hex numbers.
            me = Math.pow(10.0,Double.parseDouble(e));
        else
            me = 1;
        for(int i = 0; i < num.length(); i++){
            char s = num.charAt(i);
        }
        if(base == 16)
            return parse16(num)*me*sign;
        else if(base == 8)
            return parse8(num)*me*sign;
        else if(base == 2)
            return parse2(num)*me*sign;
        else if(base == 10)
            return parse10(num)*me*sign;
        return Double.NaN;
    }
    private static double parse16(String hex){
        String digits = "0123456789ABCDEF";
        hex = hex.toUpperCase();
        double val = 0;
        int i = 0;
        while(i < hex.length())
        {
            char c = hex.charAt(i);
            if(c == '.') {
                i++;
                break;
            }
            int d = digits.indexOf(c);
            val = 16*val + d;
            i++;
        }
        int power = 1;
        while(i < hex.length()){
            char c = hex.charAt(i);
            int d = digits.indexOf(c);
            power *= 16;
            val = 16*val + d;
            i++;
        }
        return val/power;
    }
    private static double parse8(String num){
        String digits = "01234567";
        num = num.toUpperCase();
        double val = 0;
        int i = 0;
        while(i < num.length())
        {
            char c = num.charAt(i);
            if(c == '.') {
                i++;
                break;
            }
            int d = digits.indexOf(c);
            val = 8*val + d;
            i++;
        }
        int power = 1;
        while(i < num.length()){
            char c = num.charAt(i);
            int d = digits.indexOf(c);
            power *= 8;
            val = 8*val + d;
            i++;
        }
        return val/power;
    }
    private static double parse2(String num){
        String digits = "01";
        num = num.toUpperCase();
        double val = 0;
        int i = 0;
        while(i < num.length())
        {
            char c = num.charAt(i);
            if(c == '.') {
                i++;
                break;
            }
            int d = digits.indexOf(c);
            val = 2*val + d;
            i++;
        }
        int power = 1;
        while(i < num.length()){
            char c = num.charAt(i);
            int d = digits.indexOf(c);
            power *= 2;
            val = 2*val + d;
            i++;
        }
        return val/power;
    }
    private static double parse10(String num){
        String digits = "0123456789";
        num = num.toUpperCase();
        double val = 0;
        int i = 0;
        while(i < num.length())
        {
            char c = num.charAt(i);
            if(c == '.') {
                i++;
                break;
            }
            int d = digits.indexOf(c);
            val = 10*val + d;
            i++;
        }
        int power = 1;
        while(i < num.length()){
            char c = num.charAt(i);
            int d = digits.indexOf(c);
            power *= 10;
            val = 10*val + d;
            i++;
        }
        return val/power;
    }
}
