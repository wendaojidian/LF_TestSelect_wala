import java.io.*;
import java.util.ArrayList;

public class file_io {
    public static File[] get_class_files(String target_path){
        File file_class_path=new File(target_path);
        File[] file_classes=file_class_path.listFiles();

        return file_classes;
    }

    public static ArrayList<String> readTxt(String txtPath) {
        File file = new File(txtPath);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //StringBuffer sb = new StringBuffer();
                ArrayList<String> signature_list= new ArrayList<>();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    String [] arr = text.split("\\s+");
                    signature_list.add(arr[1]);
                }
                return signature_list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void writeTxt(String txtPath,ArrayList<String> signature_list){
        String content="";
        for(String signature:signature_list){
            content+=signature+"\n";
        }
        FileOutputStream fileOutputStream = null;
        File file = new File(txtPath);
        try {
            if(file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writedot(String txtPath,String words){
        FileOutputStream fileOutputStream = null;
        File file = new File(txtPath);
        try {
            if(file.exists()){
                //判断文件是否存在，如果不存在就新建一个txt
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(words.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<String> readdot(String dotPath) {
        File file = new File(dotPath);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                //StringBuffer sb = new StringBuffer();
                ArrayList<String> signature_list= new ArrayList<>();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    signature_list.add(text);
                }
                return signature_list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
