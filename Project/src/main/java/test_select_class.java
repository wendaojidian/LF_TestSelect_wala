import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import javafx.application.Application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class test_select_class {
    public static Map<String, ArrayList<String>> get_pred(ArrayList<String> node_list){
        Map<String, ArrayList<String>> dict_ori=new HashMap<String,ArrayList<String>>();
        //arr[1]:调用者,arr[3]:被调用者
        for(String class_ori:node_list){
            String [] arr =class_ori.split("\\s+");
            if(dict_ori.containsKey(arr[3])){
                dict_ori.get(arr[3]).add(arr[1]);
            }
            else{
                ArrayList<String> pred_tmp=new ArrayList<>();
                pred_tmp.add(arr[1]);
                dict_ori.put(arr[3],pred_tmp);
            }
        }
        return dict_ori;
    }

    @org.jetbrains.annotations.NotNull
    public static Map<String,ArrayList<String>> get_pred_test(Map<String, ArrayList<String>> dict_ori, ArrayList<String> class_all){
        Map<String, ArrayList<String>> dict_test=new HashMap<String,ArrayList<String>>();
        //arr[1]:调用者,arr[3]:被调用者
        for(String class_tmp:class_all){
            String [] arr =class_tmp.split("\\s+");
            if(arr.length>3){
                //System.out.println(arr[3]+"\t"+arr[1]+"\t"+dict_ori.get(arr[3]));
                if(dict_test.containsKey(arr[3])){
                    if(dict_ori.get(arr[3])!=null){
                        if(!dict_ori.get(arr[3]).contains(arr[1]))
                            dict_test.get(arr[3]).add(arr[1]);
                    }
                    else{
                        dict_test.get(arr[3]).add(arr[1]);
                    }

                }
                else if(dict_ori.get(arr[3])!=null){
                    if(!dict_ori.get(arr[3]).contains(arr[1])){
                        ArrayList<String> pred_tmp=new ArrayList<>();
                        pred_tmp.add(arr[1]);
                        dict_test.put(arr[3],pred_tmp);
                    }
                }
                else if(dict_ori.get(arr[3])==null){
                    ArrayList<String> pred_tmp=new ArrayList<>();
                    pred_tmp.add(arr[1]);
                    dict_test.put(arr[3],pred_tmp);
                }

            }

        }
        return dict_test;
    }

    public static void print_dict(Map<String, ArrayList<String>> dict_ori){
        Iterator<Map.Entry<String,ArrayList<String>>> it=dict_ori.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,ArrayList<String>> entry_tmp=it.next();
            System.out.println(entry_tmp.getKey());
            for(String pred:entry_tmp.getValue()){
                System.out.print(pred+"\t");
            }
            System.out.println("\n");
        }
    }
    public static String get_dot_String(Map<String, ArrayList<String>> dict_ori,String file_name){
        String dot_string="digraph "+file_name+" {\n";
        Iterator<Map.Entry<String,ArrayList<String>>> it=dict_ori.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,ArrayList<String>> entry_tmp=it.next();
            for(String pred:entry_tmp.getValue()){
                dot_string+="\t\""+entry_tmp.getKey() + "\" -> \"" + pred+"\"\n";;
            }
        }
        dot_string+="}";
        return dot_string;
    }

    public static ArrayList<String> search_class_list(Map<String, ArrayList<String>> dict_test,Map<String, ArrayList<String>> dict_ori,ArrayList<String> signature_list_test,ArrayList<String> signature_pass,String sig){
        signature_pass.add(sig);
        if(dict_test.get(sig)!=null){
            for(String sig_test:dict_test.get(sig)){
                if(!signature_list_test.contains(sig_test)){
                    signature_list_test.add(sig_test);
                }
            }
        }
        if(dict_ori.get(sig)!=null){
            for(String sig_other:dict_ori.get(sig)){
                if(!signature_pass.contains(sig_other)){
                    signature_list_test=search_class_list(dict_test,dict_ori,signature_list_test,signature_pass,sig_other);
                }
            }
        }

        return signature_list_test;
    }

    public static void test_select_class(String target_path, String change_info_path) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        String class_ori_name=target_path+"/classes/net/mooctest";
        String class_test_name=target_path+"/test-classes/net/mooctest";
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = new BufferedReader(new InputStreamReader(rt.exec("jdeps -v -e net.mooctest.* "+class_ori_name).getInputStream()));
        String line=null;

        //生产程序类依赖关系
        ArrayList<String> class_ori_list=new ArrayList<>();
        while ((line=br.readLine())!=null) {
            class_ori_list.add(line);
        }
//        for(String class_ori:class_ori_list){
//            System.out.println(class_ori);
//        }
        //key:被调用者,value:调用者
        Map<String, ArrayList<String>> dict_ori=get_pred(class_ori_list);
        //print_dict(dict_ori);


        //生成测试依赖图：
        BufferedReader br_all = new BufferedReader(new InputStreamReader(rt.exec("jdeps -v -e net.mooctest.* "+class_ori_name+" "+class_test_name).getInputStream()));
        ArrayList<String> class_all_list=new ArrayList<>();
        while ((line=br_all.readLine())!=null) {
            class_all_list.add(line);
        }
//        for(String class_all:class_all_list){
//            System.out.println(class_all);
//        }
//        System.out.println("\n");
        Map<String, ArrayList<String>> dict_test=get_pred_test(dict_ori,class_all_list);
        //print_dict(dict_test);

        ArrayList<String> signature_list_changeinfo_ori=file_io.readTxt(change_info_path);
        ArrayList<String> signature_list_changeinfo=new ArrayList<String>();
        for(String signature:signature_list_changeinfo_ori){
            String[] signature_split_tmp=signature.split("\\.");
            String sig_tmp=signature_split_tmp[0]+"."+signature_split_tmp[1]+"."+signature_split_tmp[2];
            if(!signature_list_changeinfo.contains(sig_tmp)){
                signature_list_changeinfo.add(signature_split_tmp[0]+"."+signature_split_tmp[1]+"."+signature_split_tmp[2]);
            }
        }
//        for(String sig:signature_list_changeinfo){
//            System.out.println(sig);
//        }

        ArrayList<String> signature_select_list=new ArrayList<String>();
        ArrayList<String> signature_pass=new ArrayList<String>();
        for(String sig:signature_list_changeinfo){
            search_class_list(dict_test,dict_ori,signature_select_list,signature_pass,sig);
        }

//        for(String sig:signature_select_list){
//            System.out.println(sig);
//        }


        File exclusionFile=new File("main/resources/exclusion.txt");
        String scope_file_path="main/resources/scope.txt";

        File [] test_classes=new File[signature_select_list.size()];
        int i=0;
        for(String sig:signature_select_list){
            String sig_class_path=target_path+"/test-classes/"+sig.replace(".","/")+".class";
            test_classes[i++]=new File(sig_class_path);
            //System.out.println(sig_class_path);
        }

        ClassLoader MY_CLASSLOADER = AnalysisScopeReader.class.getClassLoader();
        AnalysisScope scope_result = AnalysisScopeReader.readJavaScope(scope_file_path,exclusionFile,MY_CLASSLOADER);

        for(File file_class:test_classes){
            scope_result.addClassFileToScope(ClassLoaderReference.Application,file_class);
        }

        ClassHierarchy cha_class_result= ClassHierarchyFactory.makeWithRoot(scope_result);

        Iterable<Entrypoint> eps_result=new AllApplicationEntrypoints(scope_result,cha_class_result);

        CHACallGraph cg_result=new CHACallGraph(cha_class_result);

        cg_result.init(eps_result);

        ArrayList<String> signature_result=new ArrayList<>();
        for(CGNode node:cg_result) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName =
                            method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    Collection<Annotation> ann=method.getAnnotations();
                    if(ann.toString().length()>45&&ann.toString().substring(17,46).equals("<Application,Lorg/junit/Test>")){
                        System.out.println(classInnerName+" "+signature+"\t");
                        signature_result.add(classInnerName+" "+signature);
                    }
                }
            }
        }
        file_io.writeTxt("selection-class.txt",signature_result);
    }

    public static void get_class_dot(String target_path, String change_info_path,String file_name,String graph_name) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        String class_ori_name = target_path + "/classes/net/mooctest";
        String class_test_name = target_path + "/test-classes/net/mooctest";
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = new BufferedReader(new InputStreamReader(rt.exec("jdeps -v -e net.mooctest.* " + class_ori_name).getInputStream()));
        String line = null;

        //生产程序类依赖关系
        ArrayList<String> class_ori_list = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            class_ori_list.add(line);
        }
//        for(String class_ori:class_ori_list){
//            System.out.println(class_ori);
//        }
        //key:被调用者,value:调用者
        Map<String, ArrayList<String>> dict_ori = get_pred(class_ori_list);
        //print_dict(dict_ori);


        //生成测试依赖图：
        BufferedReader br_all = new BufferedReader(new InputStreamReader(rt.exec("jdeps -v -e net.mooctest.* " + class_ori_name + " " + class_test_name).getInputStream()));
        ArrayList<String> class_all_list = new ArrayList<>();
        while ((line = br_all.readLine()) != null) {
            class_all_list.add(line);
        }
//        for(String class_all:class_all_list){
//            System.out.println(class_all);
//        }
//        System.out.println("\n");
        Map<String, ArrayList<String>> dict_test = get_pred_test(dict_ori, class_all_list);
        String dot_String=get_dot_String(dict_test,graph_name);
        System.out.println(dot_String);
        file_io.writedot(file_name+".dot",dot_String);


    }

}
