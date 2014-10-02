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
		
		Map<String,Double> idfMap = new HashMap<String, Double>();
		Map<String, Integer> wordMap = new HashMap<String, Integer>();	//�ʺ���ŵ�map
		ArrayList<String> wordArrayList = new ArrayList<String>();
		ArrayList<Integer> numPostPerTheme = new ArrayList<Integer>();
		Map<Integer, Integer> postToThemeMap = new HashMap<Integer, Integer>();	//tfidf������ж�Ӧ������
		ArrayList<String> postArrayList = new ArrayList<String>();
		
		
		int wordMapIndex=0;		//�ʵ������ṹ�����õ����Ǵ���
		int countPost=0;
		int countTheme=10;
		//int postIndex = 0;
		String str = "�����Էִʹ���һЩֹͣ��";
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
        
		
		for(int i=0;i<post.length;i++){			//�õ�һ����-��ŵ�map
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
		
		for(int i = 0;i<postArrayList.size();i++){		//�õ�һ����Ƶ���ľ���
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
		
		
		for(int j=0;j<wordMapIndex;j++){			//�õ�ÿ�����ڶ��ٸ������г��ֹ�������������idf��ֵ��
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
		while(iterator.hasNext()){		//����ÿ���ʵ�idfֵ
			String word = iterator.next();
			
			double d = idfMap.get(word).doubleValue();
			d=Math.log((countPost)/(1+d));
			
			idfMap.put(word, d);
			
		}
		/*
		 * 	10������֤
		 * 	
		 */
		ArrayList<Double> correctNumNBD = new ArrayList<Double>();
		ArrayList<Double> correctNumNBCD = new ArrayList<Double>();
		ArrayList<Double> correctNumNBCG = new ArrayList<Double>();
		
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
				if((i%10)==k){			//���Լ�
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
				else{					//ѵ����
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
			
			//����NBD
			
			
		}
		double sumNBD = 0;
		double sumNBCD = 0;
		double sumNBCG = 0;
		for(int i = 0;i<correctNumNBD.size();i++){
			sumNBD = sumNBD+ correctNumNBD.get(i);
			sumNBCD = sumNBCD + correctNumNBCD.get(i);
			sumNBCG = sumNBCG + correctNumNBCG.get(i);
		}
		
		double averageNBD = sumNBD/correctNumNBD.size();
		double averageNBCD = sumNBCD/correctNumNBD.size();
		double averageNBCG = sumNBCG/correctNumNBD.size();
		sumNBD = 0;
		sumNBCD = 0;
		sumNBCG = 0;
		for(int i = 0;i<10;i++){
			double v1=correctNumNBD.get(i);
			double v2 = correctNumNBCD.get(i);
			double v3 = correctNumNBCG.get(i);
			sumNBD = sumNBD + (v1-averageNBD)*(v1-averageNBD);
			sumNBCD = sumNBCD + (v2-averageNBCD)*(v2- averageNBCD);
			sumNBCG = sumNBCG + (v3-averageNBCG)*(v3-averageNBCG);
		}
		double varianceNBD = sumNBD/correctNumNBD.size();
		double varianceNBCD = sumNBCD/correctNumNBD.size();
		double varianceNBCG = sumNBCG/correctNumNBD.size();
		System.out.println("NBD ƽ��ֵΪ"+averageNBD+",����Ϊ "+varianceNBD);
		System.out.println("NBCD ƽ��ֵΪ"+averageNBCD+",����Ϊ "+varianceNBCD);
		System.out.println("NBCG ƽ��ֵΪ"+averageNBCG+",����Ϊ "+varianceNBCG);
		System.out.println("--�������--");
		
	}
}
