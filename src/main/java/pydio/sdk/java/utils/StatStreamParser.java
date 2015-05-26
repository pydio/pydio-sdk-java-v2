package pydio.sdk.java.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import pydio.sdk.java.model.ChangeProcessor;

/**
 * Created by pydio on 04/05/2015.
 */
public class StatStreamParser {
    InputStream in;
    int state;
    private final int READING_KEY = 0;
    private final int READING_VALUE = 1;
    private final int VALUE_READ = 2;

    public StatStreamParser(InputStream in){
        this.in = in;
    }
    public void parse(ChangeProcessor p, String charset) throws Exception {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("}");
        Scanner sc = new Scanner(in, charset);
        sc.useDelimiter("\":\\{");
        System.out.println(sc.delimiter());

        String line = sc.next().trim();
        System.out.println(line);
        if(line.length() == 0 || !line.startsWith("{\"")) throw new Exception("");
        keys.add(line.substring(2));
        state = READING_VALUE;

        state = READING_VALUE;
        for(;;){
            switch (state){
                case READING_KEY:
                    sc.useDelimiter("\":\\{");
                    System.out.println();
                    line = sc.nextLine().trim();
                    System.out.println(line);
                    if(line.length() == 0 && !sc.hasNext()){
                        if(keys.size() == 0) return;
                        else throw new Exception("");
                    }
                    if(!line.startsWith(",\"")){
                        throw  new Exception("");
                    }
                    keys.add(line.substring(2));
                    state = READING_VALUE;
                    break;

                case READING_VALUE:
                    sc.useDelimiter("\\}");
                    System.out.println(sc.delimiter());

                    line = sc.nextLine();
                    System.out.println(line);
                    if(line.length() == 0){
                        throw new Exception("");
                    }
                    String[] elements = line.split(",");
                    String[] stat = new String[elements.length+1];
                    for(int i = 0; i < elements.length; i++){
                        stat[i+1] = elements[i].split(":")[1];
                    }
                    stat[0] = keys.remove(keys.size() - 1);
                    p.process(stat);
                    state = READING_KEY;
                    break;
                default:
                    break;
            }
        }
    }
}
