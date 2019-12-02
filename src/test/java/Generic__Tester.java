
import java.util.Arrays;
import java.util.stream.Collectors;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author riche
 */
public class Generic__Tester {

    public static void main(String[] args) {
        final String test = "${com.sun.aas.instanceRootURI}/applications/one/";
        
        final String[] split = test.split("/", -1);
        
        final String result = Arrays.stream(split).map(var -> var.startsWith("${") ? System.getProperty(var.replaceAll("[${}]", ""), "funfo") : var).collect(Collectors.joining("/"));
        
        System.out.println(test.replaceAll("[${}]", ""));
        System.out.println(result);
    }
}
