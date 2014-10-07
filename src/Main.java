import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;


public class Main {

	public static void main(String[] args) throws IOException{
		//System.out.println("helloworld");
		Map<String,Double> idfMap = new HashMap<String, Double>();
		Map<String, Integer> wordMap = new HashMap<String, Integer>();	//词和序号的map
		ArrayList<String> wordArrayList = new ArrayList<String>();
		ArrayList<Integer> numPostPerTheme = new ArrayList<Integer>();
		Map<Integer, Integer> postToThemeMap = new HashMap<Integer, Integer>();	//tfidf矩阵的行对应的主题
		ArrayList<String> postArrayList = new ArrayList<String>();
		
		
		int wordMapIndex=0;		//词的索引结构，最后得到的是词数
		int countPost=0;
		int countTheme=10;
		//int postIndex = 0;
		String str = "啊测试分词工具一些停止词";
		String directory = "data\\";
		String basketball=directory+"Basketball.txt";
		String computer=directory+"D_Computer.txt";
		String fleaMarket = directory+"FleaMarket.txt";
		String girls = directory + "Girls.txt";
		String jobExpress = directory+"JobExpress.txt";
		String mobile = directory + "Mobile.txt";
		String stock = directory + "Stock.txt";
		String suggestion = directory+"V_Suggestions.txt";
		String warAndPeace = directory+"WarAndPeace.txt";
		String WorldFootball = directory + "WorldFootball.txt";
		
		String[] post = {basketball,computer,fleaMarket,girls,jobExpress,mobile,stock,suggestion,
				warAndPeace,WorldFootball};
        
		
		for(int i=0;i<post.length;i++){			//得到一个词-序号的map
			File file = new File(post[i]);
			Scanner input = new Scanner(file);
			int postPerTheme=0;
	        while(input.hasNext()){
	        	postPerTheme++;
	        	postToThemeMap.put(countPost, i);
	        	countPost++;
	        	str = input.nextLine();
	        	postArrayList.add(str);
	        	StringReader reader = new StringReader(str);
	        	IKSegmenter ik = new IKSegmenter(reader,true);
	        	
	        	Lexeme lexeme = null;
	        	while((lexeme = ik.next())!=null){
	        		String word = lexeme.getLexemeText();
	        		
	        		if(!wordMap.containsKey(word)){
	        			wordMap.put(word, wordMapIndex);
	        			
	        			wordArrayList.add(word);
	        			wordMapIndex++;
	        		}
	        	}
	        }
	        numPostPerTheme.add(postPerTheme);
	        input.close();
		}
		
		double[][] tfidfMatrix = new double[countPost][wordMapIndex];
		for(int i = 0;i<countPost;i++)
			for(int j = 0;j<wordMapIndex;j++)
				tfidfMatrix[i][j] = 0;
		
		for(int i = 0;i<postArrayList.size();i++){		//得到一个词频数的矩阵。
			String string = postArrayList.get(i);
			StringReader reader = new StringReader(string);
			IKSegmenter ik = new IKSegmenter(reader, true);
			Lexeme lx = null;
			while((lx = ik.next())!=null){
				String word = lx.getLexemeText();
				int column = wordMap.get(word).intValue();
				tfidfMatrix[i][column] = tfidfMatrix[i][column]+1;
			}
		}
		
		
		for(int j=0;j<wordMapIndex;j++){			//得到每个词在多少个帖子中出现过，以用来计算idf的值。
			String word = wordArrayList.get(j);
			if(!idfMap.containsKey(word)){
				idfMap.put(word, 0.0);
			}
			double sum = 0;
			for(int i=0;i<countPost;i++){
				if(tfidfMatrix[i][j]>0)
					sum = sum+1;
			}
			idfMap.put(word, sum);
		}
		
		Set<String> set = idfMap.keySet();
		
		Iterator<String> iterator = set.iterator();
		while(iterator.hasNext()){		//计算每个词的idf值
			String word = iterator.next();
			
			double d = idfMap.get(word).doubleValue();
			d=Math.log((countPost)/(1+d));
			
			idfMap.put(word, d);
			
		}
		/*
		 * 	10交叉验证
		 * 	
		 */
		
		ArrayList<Double> correctNumLR = new ArrayList<Double>();
		
		
		for(int k = 0;k<10;k++){				
			
			Map<Integer, Integer> testPostThemeMap = new HashMap<Integer, Integer>();
			Map<Integer, Integer> trainPostThemeMap = new HashMap<Integer, Integer>();
			
			Map<Integer, Integer> testThemePostNumMap = new HashMap<Integer, Integer>();
			Map<Integer, Integer> trainThemePostNumMap = new HashMap<Integer, Integer>();
			
			
			int testTotalPost = countPost/10;
			int trainTotalPost = countPost-testTotalPost;
			double[][] trainMatrix = new double[trainTotalPost][wordMapIndex];
			int flagTestRow = 0;
			int flagTrainRow = 0;
			double[][] testMatrix = new double[testTotalPost][wordMapIndex];
			for(int i = 0;i<countPost;i++){		
				if((i%10)==k){			//测试集
					for(int j = 0;j<wordMapIndex;j++){
						
						testMatrix[flagTestRow][j] = tfidfMatrix[i][j];  
						
					}
					int whichTheme = postToThemeMap.get(i).intValue();
					testPostThemeMap.put(flagTestRow, whichTheme);
					if(testThemePostNumMap.containsKey(whichTheme)){
						int numPost = testThemePostNumMap.get(whichTheme).intValue();
						numPost = numPost+1;
						testThemePostNumMap.put(whichTheme, numPost);
					}
					else{
						testThemePostNumMap.put(whichTheme, 1);
					}
					flagTestRow++;
				}
				else{					//训练集
					for(int j = 0;j<wordMapIndex;j++){
						trainMatrix[flagTrainRow][j] = tfidfMatrix[i][j];
					}
					int whichTheme = postToThemeMap.get(i).intValue();
					trainPostThemeMap.put(flagTrainRow, whichTheme);
					if(trainThemePostNumMap.containsKey(whichTheme)){
						int numPost = trainThemePostNumMap.get(whichTheme).intValue();
						numPost = numPost+1;
						trainThemePostNumMap.put(whichTheme, numPost);
					}
					else{
						trainThemePostNumMap.put(whichTheme, 1);
					}
					flagTrainRow++;
				}
			}
			
			
			System.out.println("准备第"+k+"次逻辑回归运算");
			LogisticRegression lr = new LogisticRegression(countPost, wordMapIndex, countTheme);
			for(int index=0;index<countTheme;index++){
				int value = trainThemePostNumMap.get(index);
				lr.setNumPerTheme(value);
			}
			lr.trainLogRegres(trainMatrix);
			double sumLR = 0;
			for(int i=0;i<testTotalPost;i++){
				int classTheme = lr.classify(testMatrix[i]);
				int realTheme = testPostThemeMap.get(i).intValue();
				if(classTheme==realTheme)
					sumLR += 1.0;
			}
			double correctRatio = sumLR/testTotalPost;
			correctNumLR.add(correctRatio);
			System.out.println("第"+k+"次准确率为："+correctRatio);
		}
		double sumLR = 0;
		for(int i = 0;i<correctNumLR.size();i++){
			sumLR += correctNumLR.get(i);
		}
		double averageLR = sumLR/correctNumLR.size();
		sumLR = 0;
		for(int i=0;i<correctNumLR.size();i++){
			double v = correctNumLR.get(i);
			sumLR += (v-averageLR)*(v-averageLR);
		}
		double variance = sumLR/correctNumLR.size();
		
		System.out.println("NBD 平均值为"+averageLR+",方差为 "+variance);

		System.out.println("--运算完成--");
		
		
	}
	
}
