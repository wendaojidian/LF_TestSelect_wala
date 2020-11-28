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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class test_select_method {
    public static ArrayList<String> search_signature(CHACallGraph cg,ArrayList<String> signature_list_select,CGNode node,ArrayList<String> signature_list_ori,ArrayList<String> signature_list_test,ArrayList<CGNode> node_has){
        Iterator node_pred_itr=cg.getPredNodes(node);
        while(node_pred_itr.hasNext()){
            CGNode node_pre= (CGNode) node_pred_itr.next();
            if(!node_has.contains(node_pre)){
                node_has.add(node_pre);
                if (signature_list_test.contains(node_pre.getMethod().getSignature())){
                    Collection<Annotation> ann=node_pre.getMethod().getAnnotations();
                    if(ann.toString().length()>45&&ann.toString().substring(17,46).equals("<Application,Lorg/junit/Test>")){
                        signature_list_select.add(node_pre.getMethod().getDeclaringClass().getName().toString()+" "+node_pre.getMethod().getSignature());
                    }
                    //System.out.println(node_pre.getMethod().getSignature()+"\t"+node_pre.getMethod().getAnnotations());

                }
                else if (signature_list_ori.contains(node_pre.getMethod().getSignature())){
                    signature_list_select=search_signature(cg,signature_list_select,node_pre,signature_list_ori,signature_list_test,node_has);
                }
            }

        }
        return signature_list_select;

    }


    public static void test_select_method(String target_path, String change_info_path) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        File exclusionFile=new File("main/resources/exclusion.txt");
        String scope_file_path="main/resources/scope.txt";

        File[] file_class_ori=file_io.get_class_files(target_path+"/classes/net/mooctest");
        File[] file_class_test=file_io.get_class_files(target_path+"/test-classes/net/mooctest");

        ArrayList<String> signature_list_changeinfo=file_io.readTxt(change_info_path);
        //System.out.println("signature_list:"+ signature_list_changeinfo.get(0));

        ClassLoader MY_CLASSLOADER = AnalysisScopeReader.class.getClassLoader();

        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scope_file_path,exclusionFile,MY_CLASSLOADER);
        //只有测试代码的图
        AnalysisScope scope_test_only = AnalysisScopeReader.readJavaScope(scope_file_path,exclusionFile,MY_CLASSLOADER);
        for(File file_class:file_class_test){
            scope_test_only.addClassFileToScope(ClassLoaderReference.Application,file_class);
        }

        ClassHierarchy cha_class_test = ClassHierarchyFactory.makeWithRoot(scope_test_only);

        Iterable<Entrypoint> eps_test=new AllApplicationEntrypoints(scope,cha_class_test);

        CHACallGraph cg_test=new CHACallGraph(cha_class_test);

        cg_test.init(eps_test);

        ArrayList<String> signature_test=new ArrayList<>();
        for(CGNode node:cg_test) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String signature = method.getSignature();
                    //System.out.println(signature);
                    signature_test.add(signature);
                }
            }
        }


        //源代码的图
        for(File file_class:file_class_ori){
            scope.addClassFileToScope(ClassLoaderReference.Application,file_class);
        }

        ClassHierarchy cha_class_ori = ClassHierarchyFactory.makeWithRoot(scope);

        Iterable<Entrypoint> eps=new AllApplicationEntrypoints(scope,cha_class_ori);

        CHACallGraph cg=new CHACallGraph(cha_class_ori);

        cg.init(eps);
        //System.out.println(cg.stream().count());

        //String[] signature_ori=new String[10000];
        ArrayList<String> signature_ori=new ArrayList<>();
        for(CGNode node:cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String signature = method.getSignature();
//                    System.out.println(signature);
                    signature_ori.add(signature);
                }
            }
        }

        //源代码+测试代码的图
        for(File file_test:file_class_test){
            scope.addClassFileToScope(ClassLoaderReference.Application,file_test);
        }

        ClassHierarchy cha_all = ClassHierarchyFactory.makeWithRoot(scope);

        Iterable<Entrypoint> eps_all=new AllApplicationEntrypoints(scope,cha_all);

        CHACallGraph cg_all=new CHACallGraph(cha_all);
        cg_all.init(eps_all);

        ArrayList<String> signature_select_list=new ArrayList<String>();
        ArrayList<CGNode> node_has=new ArrayList<CGNode>();
        for(CGNode node:cg_all){
            if(signature_list_changeinfo.contains(node.getMethod().getSignature())){
//                System.out.println(signature_select_list);
//                System.out.println(node.getMethod().getSignature());
                signature_select_list=search_signature(cg_all,signature_select_list,node,signature_ori,signature_test,node_has);
            }
        }
        for(String signature:signature_select_list){
            System.out.println(signature);
        }
        file_io.writeTxt("selection-method.txt",signature_select_list);

    }
    public static void get_method_dot(String target_path, String change_info_path,String file_name,String graph_name) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        File exclusionFile=new File("main/resources/exclusion.txt");
        String scope_file_path="main/resources/scope.txt";

        File[] file_class_ori=file_io.get_class_files(target_path+"/classes/net/mooctest");
        File[] file_class_test=file_io.get_class_files(target_path+"/test-classes/net/mooctest");

        ArrayList<String> signature_list_changeinfo=file_io.readTxt(change_info_path);
        //System.out.println("signature_list:"+ signature_list_changeinfo.get(0));

        ClassLoader MY_CLASSLOADER = AnalysisScopeReader.class.getClassLoader();

        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scope_file_path,exclusionFile,MY_CLASSLOADER);
        //只有测试代码的图
        AnalysisScope scope_test_only = AnalysisScopeReader.readJavaScope(scope_file_path,exclusionFile,MY_CLASSLOADER);
        for(File file_class:file_class_test){
            scope_test_only.addClassFileToScope(ClassLoaderReference.Application,file_class);
        }

        ClassHierarchy cha_class_test = ClassHierarchyFactory.makeWithRoot(scope_test_only);

        Iterable<Entrypoint> eps_test=new AllApplicationEntrypoints(scope,cha_class_test);

        CHACallGraph cg_test=new CHACallGraph(cha_class_test);

        cg_test.init(eps_test);

        ArrayList<String> signature_test=new ArrayList<>();
        for(CGNode node:cg_test) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String signature = method.getSignature();
                    //System.out.println(signature);
                    signature_test.add(signature);
                }
            }
        }


        //源代码的图
        for(File file_class:file_class_ori){
            scope.addClassFileToScope(ClassLoaderReference.Application,file_class);
        }

        ClassHierarchy cha_class_ori = ClassHierarchyFactory.makeWithRoot(scope);

        Iterable<Entrypoint> eps=new AllApplicationEntrypoints(scope,cha_class_ori);

        CHACallGraph cg=new CHACallGraph(cha_class_ori);

        cg.init(eps);
        //System.out.println(cg.stream().count());

        //String[] signature_ori=new String[10000];
        ArrayList<String> signature_ori=new ArrayList<>();
        for(CGNode node:cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String signature = method.getSignature();
//                    System.out.println(signature);
                    signature_ori.add(signature);
                }
            }
        }

        //源代码+测试代码的图
        for(File file_test:file_class_test){
            scope.addClassFileToScope(ClassLoaderReference.Application,file_test);
        }

        ClassHierarchy cha_all = ClassHierarchyFactory.makeWithRoot(scope);

        Iterable<Entrypoint> eps_all=new AllApplicationEntrypoints(scope,cha_all);

        String method_2dot="digraph "+graph_name+" {\n";
        CHACallGraph cg_all=new CHACallGraph(cha_all);
        cg_all.init(eps_all);
        for(CGNode node:cg_all) {
            if(signature_ori.contains(node.getMethod().getSignature())) {
                Iterator node_pred_itr = cg_all.getPredNodes(node);
                while (node_pred_itr.hasNext()) {
                    CGNode node_pre = (CGNode) node_pred_itr.next();
                    if(signature_test.contains(node_pre.getMethod().getSignature())){
                        method_2dot+="\t\""+node.getMethod().getSignature() + "\" -> \"" + node_pre.getMethod().getSignature()+"\"\n";
                        //System.out.println("\t\""+node.getMethod().getSignature() + "\" -> \"" + node_pre.getMethod().getSignature());
                    }
                }
            }
        }
        method_2dot+="}";
        file_io.writedot(file_name+".dot",method_2dot);

    }
}
