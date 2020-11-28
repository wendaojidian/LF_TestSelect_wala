import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.config.FileOfClasses;


public class wala_main {
    public static void main(String[] args) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
//        if(args[0].equals("-m"))
//            test_select_method.test_select_method(args[1],args[2]);
//        else if(args[0].equals("-c"))
//            test_select_class.test_select_class(args[1],args[2]);
////        test_select_class.test_select_class("input/target_1","input/target_1/change_info.txt");
//        ArrayList<String> dot_file_string=file_io.readdot("input/class-CMD-cfa.dot");
//        for(String str:dot_file_string){
//            System.out.println(str);
//        }
//        file_io.writeTxt("dot_try.dot",dot_file_string);
        test_select_method.get_method_dot("input/target_1","input/target_1/change_info.txt","method-ALU","ALU_method");
        test_select_method.get_method_dot("input/target_2","input/target_2/change_info.txt","method-DataLog","DataLog_method");
        test_select_method.get_method_dot("input/target_3","input/target_3/change_info.txt","method-BinaryHeap","BinaryHeap_method");
        test_select_method.get_method_dot("input/target_4","input/target_4/change_info.txt","method-NextDay","NextDay_method");
        test_select_method.get_method_dot("input/target_5","input/target_5/change_info.txt","method-MoreTriangle","MoreTriangle_method");

        test_select_class.get_class_dot("input/target_1","input/target_1/change_info.txt","class-ALU","ALU_class");
        test_select_class.get_class_dot("input/target_2","input/target_2/change_info.txt","class-DataLog","DataLog_class");
        test_select_class.get_class_dot("input/target_3","input/target_3/change_info.txt","class-BinaryHeap","BinaryHeap_class");
        test_select_class.get_class_dot("input/target_4","input/target_4/change_info.txt","class-NextDay","NextDay_class");
        test_select_class.get_class_dot("input/target_5","input/target_5/change_info.txt","class-MoreTriangle","MoreTriangle_class");

    }
}


        