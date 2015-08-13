import java.util.ArrayList;


public class LogisticRegression {
	
	double[][] theta;
	int totalPost;
	int totalKeywords;
	int totalTheme;
	ArrayList<Integer> numPostEveryTheme;
	
	public LogisticRegression(int totalPost,int totalKeywords,int totalTheme){
		this.totalPost = totalPost;
		this.totalKeywords = totalKeywords;
		this.totalTheme = totalTheme;
		theta = new double[totalTheme][totalKeywords];
		numPostEveryTheme = new ArrayList<Integer>();
		init();
	}
	
	public void setNumPerTheme(int sum){
		numPostEveryTheme.add(sum);
	}
	
	public void init(){
		for(int i=0;i<totalTheme;i++)
			for(int j=0;j<totalKeywords;j++)
				theta[i][j]=1;
	}
	
	//sigmoid函数
	public double sigmoid(double x){
		double d=1.0/(1+Math.exp(-x));
		return d;
	}
	//训练模型
	public void trainLogRegres(double[][] trainMatrix){
		int flagRow=0;
		for(int i=0;i<totalTheme;i++){
			double alpha=2;
			int count=0;
			int numPost = numPostEveryTheme.get(i);
			while(count<40){
				
				for(int k=0;k<trainMatrix.length;k++){
					alpha=4.0/(1+count+k)+0.01;
					double lire = innerProduct(theta[i], trainMatrix[k]);
					double out = sigmoid(lire);
					double error = 0;
					if((k>=flagRow)&&k<(flagRow+numPost)){
						error = 1-out;
						
					}
					
					else {
						error=0-out;
					}
					for(int d=0;d<theta[i].length;d++){		//采用随机下降梯度算法更新权重。
						theta[i][d]=theta[i][d]+alpha*error*trainMatrix[k][d];
					}
				}
				count++;
			}
			flagRow = flagRow+numPost;
		}
	}
	
	public double innerProduct(double[] w,double[] f){		//计算两个向量的乘积
		double sum=0;
		for(int i=0;i<w.length;i++)
			sum+=w[i]*f[i];
		return sum;
	}
	
	public int classify(double[] testArray){		//分类预测
		double[] probability = new double[totalTheme];
		for(int i=0;i<totalTheme;i++)
			probability[i]=0;
		for(int i=0;i<totalTheme;i++){
			probability[i]=innerProduct(theta[i], testArray);
		}
		int max = 0;
		for(int i=0;i<totalTheme;i++){
			if(probability[max]<probability[i])
				max=i;
		}
		return max;
	}
	
}



