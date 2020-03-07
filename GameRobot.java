package xyz.biurua.llk;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 * 使用方法：按TODO的指示根据需要修改参数，截取游戏界面（尽量只截取游戏界面），编译并运行程序，切换到游戏界面，在游戏执行完成前尽量不要移动鼠标
 */
public class GameRobot {
	public static void main(String[] args) throws Exception {
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Image img=null;
		try {
			img = (Image)systemClipboard.getData(DataFlavor.imageFlavor);
		} catch (Exception e1) {
			System.out.println("在剪贴板中没有发现图片，请先截取图片");
			return;
		}
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		System.out.println("图像宽"+width+"像素，高"+height+"像素");
		BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		bimg.getGraphics().drawImage(img, 0, 0, null);
		//一行的每个格子的起止
		List<int[]> squaresInTheRow = getSquaresStartAndEndPointsInTheRow(bimg,getTheRowPixelList(bimg, 100));
		//一列的每个格子的起止
		List<int[]> squaresInTheColumn = getSquaresStartAndEndPointsInTheColumn(bimg,getTheColumnPixelList(bimg, 100));
		System.out.println("游戏界面有"+squaresInTheColumn.size()+"行"+squaresInTheRow.size()+"列格子");
		//显示界面的分割信息，用于调试
//		show(bimg,squaresInTheRow,squaresInTheColumn);
		//用于保存不同图案的格子，将同种格子简化为数字
		List<Square> allDifferentSquares=new ArrayList<>();
		//统计各种图标的数量，用于判断是否存在奇数个的图标（存在则表示识别出现问题）
		Map<Integer,Integer> iconAndNumber=new LinkedHashMap<>();
		//保存识别后的结果
		int[][] model=new int[squaresInTheColumn.size()][squaresInTheRow.size()];
		int numberOfSquaresNotEliminated=0;
		long startTimeOfIconRecognition = System.currentTimeMillis();
		for(int y=0;y<squaresInTheColumn.size();y++) {
			outer:
			for(int x=0;x<squaresInTheRow.size();x++) {
				//保存当前格子的颜色和数量
				Map<Integer,Integer> colorAndNumber=new HashMap<>();
				//用于保存当前格子有多少不同的像素，如果像素数比较少，可以判断为空格
				Set<Integer> set=new HashSet<>();
				int l=squaresInTheRow.get(x)[0];
				int r=squaresInTheRow.get(x)[1];
				int u=squaresInTheColumn.get(y)[0];
				int d=squaresInTheColumn.get(y)[1];
				for(int i=l;i<r;i++) {
					for(int j=u;j<d;j++) {
						int rgb=bimg.getRGB(i, j);
						colorAndNumber.compute(rgb, (k,v)->v==null?1:v+1);
						set.add(rgb);
					}
				}
				//这个格子缺少足够的不同像素数，可认为是空格
				if(set.size()<40) {
					model[y][x]=-1;
					continue;
				}
				numberOfSquaresNotEliminated++;
				Square square = new Square(colorAndNumber);
				System.out.print(Arrays.toString(square.rgbAverageValue));
				
				
				//用于调试
//				if(x==6&&y==1) {
//					squareTemp1=square;
//				}
//				if(x==6&&y==5) {
//					squareTemp2=square;
//				}
//				if(squareTemp1!=null&&squareTemp2!=null) {
//					System.out.println();
//					System.out.println(Arrays.toString(squareTemp1.rgbAverageValue));
//					System.out.println(Arrays.toString(squareTemp2.rgbAverageValue));
//					System.out.println(squareTemp1.equals(squareTemp2));
//					System.out.println(squareTemp2.equals(squareTemp1));
//					squareTemp1=null;
//					System.exit(0);
//				}
				for(int i=0,size=allDifferentSquares.size();i<size;i++) {
					if(allDifferentSquares.get(i).equals(square)) {
						model[y][x]=i;
						iconAndNumber.compute(i,(k,v)->v+1);
						continue outer;
					}
				}
				allDifferentSquares.add(square);
				model[y][x]=allDifferentSquares.size()-1;
				iconAndNumber.put(allDifferentSquares.size()-1, 1);
			}
			System.out.println();
		}
		long endTimeOfIconRecognition = System.currentTimeMillis();
		System.out.println("图标识别完成，耗时"+(endTimeOfIconRecognition-startTimeOfIconRecognition)/1000+"秒");
		//用于调试
//		System.out.println("相似图标在比较时的平均耗时为"+totalTimeSpentComparingSimilarIcons/comparisonTimesOfSimilarIcons+"毫秒");
//		System.out.println("不相似图标在比较时的平均耗时为"+totalTimeSpentComparingDifferentIcons/comparisonTimesOfDifferentIcons+"毫秒");
//		System.out.println("相似图标的最大完全匹配度"+maxMatchDegreeOfSimilarIcons);
//		System.out.println("相似图标的最小完全匹配度"+minMatchDegreeOfSimilarIcons);
//		System.out.println("相似图标的平均完全匹配度"+totalMatchDegreeOfSimilarIcons/comparisonTimesOfSimilarIcons);
//		System.out.println("不相似图标的最大完全匹配度"+maxMatchDegreeOfDifferentIcons);
//		System.out.println("不相似图标的最小完全匹配度"+minMatchDegreeOfDifferentIcons);
//		System.out.println("不相似图标的平均完全匹配度"+totalMatchDegreeOfDifferentIcons/comparisonTimesOfDifferentIcons);
		System.out.println("得到的模型：");
		for(int i=0;i<model.length;i++) {
			for(int j=0;j<model[i].length;j++) {
				System.out.print("["+model[i][j]+(model[i][j]!=-1&&model[i][j]<10?" ]":"]")+",");
			}
			System.out.println();
		}
		System.out.println("各种图标和对应的数量：");
		iconAndNumber.forEach((k,v)->System.out.println("图标"+k+"有"+v+"个，"+(v%2==0?"偶数":"奇数")));
		
		boolean oddNumberOfIconExist=false;
		Collection<Integer> values=iconAndNumber.values();
		for(Integer numberOfIcon:values) {
			oddNumberOfIconExist|=(numberOfIcon%2!=0);
		}
		if(oddNumberOfIconExist) {
			System.out.println("存在数量为奇数的图标");
			return;
		}
		long startTimeOfConnectionStepCalculation = System.currentTimeMillis();
		// TODO 宠物连连看有生命限制，应使用[getMoreConnectionStepList]方法
		List<int[]> connectionStepList=getConnectionStepList(model);
		long endTimeOfConnectionStepCalculation = System.currentTimeMillis();
		System.out.println("连接步骤计算完成，耗时"+(endTimeOfConnectionStepCalculation-startTimeOfConnectionStepCalculation)/1000+"秒，一共有"
									+connectionStepList.size()+"步连接操作，程序将在5秒后自动执行游戏，请切换到游戏界面...");
		Thread.sleep(5000);
		
		//识别截取的游戏界面在屏幕中的位置
		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		Robot robot = new Robot();
        BufferedImage screen = robot.createScreenCapture(new Rectangle(screenDimension));
        int[] screenshotStartPoint=new int[2];
        int randomY=(int)(Math.random()*100)+50;
        List<Integer> getTheRowPixelList=getTheRowPixelList(bimg, randomY);
        boolean findGameInterface=false;
        oouter:
        for(int i=0;i<screen.getHeight();i++) {
        	outer:
        	for(int j=0;j<screen.getWidth()-getTheRowPixelList.size();j++) {
        		//但凡出现相同像素，就依次进行比较
        		if(screen.getRGB(j, i)==getTheRowPixelList.get(0)) {
        			for(int k=1;k<getTheRowPixelList.size();k++) {
        				//如果接下来有一个像素不相同，就结束比较
        				if(screen.getRGB(j+k, i)!=getTheRowPixelList.get(k)) {
        					continue outer;
        				}
        			}
        			//完全匹配
        			screenshotStartPoint[0]=j;
        			screenshotStartPoint[1]=i-randomY;
        			findGameInterface=true;
        			break oouter;
        		}
        	}
        }
        if(!findGameInterface) {
        	System.out.println("没有找到游戏界面，程序退出");
        	//提醒
        	java.awt.Toolkit.getDefaultToolkit().beep();
        	return;
        }
        while(true) {
        	connectionStepList.forEach(arr->{
        		int square1_horizontalIndex=arr[0];
        		int square1_verticalIndex=arr[1];
        		int l=squaresInTheRow.get(square1_horizontalIndex)[0];
        		int r=squaresInTheRow.get(square1_horizontalIndex)[1];
        		int u=squaresInTheColumn.get(square1_verticalIndex)[0];
        		int d=squaresInTheColumn.get(square1_verticalIndex)[1];
        		robot.mouseMove(screenshotStartPoint[0]+l+(r-l)/2, screenshotStartPoint[1]+u+(d-u)/2);
        		// 按下鼠标
        		robot.mousePress(MouseEvent.BUTTON1_MASK);
        		// 释放鼠标
        		robot.mouseRelease(MouseEvent.BUTTON1_MASK);
        		try {
        			Thread.sleep(200);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        		int square2_horizontalIndex=arr[2];
        		int square2_verticalIndex=arr[3];
        		l=squaresInTheRow.get(square2_horizontalIndex)[0];
        		r=squaresInTheRow.get(square2_horizontalIndex)[1];
        		u=squaresInTheColumn.get(square2_verticalIndex)[0];
        		d=squaresInTheColumn.get(square2_verticalIndex)[1];
        		robot.mouseMove(screenshotStartPoint[0]+l+(r-l)/2, screenshotStartPoint[1]+u+(d-u)/2);
        		// 按下鼠标
        		robot.mousePress(MouseEvent.BUTTON1_MASK);
        		// 释放鼠标
        		robot.mouseRelease(MouseEvent.BUTTON1_MASK);
        		try {
        			Thread.sleep(200);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
        	});
        	numberOfSquaresNotEliminated-=(connectionStepList.size()*2);
			if(numberOfSquaresNotEliminated==0) break;
			// TODO 游戏进入僵局重新排列，等待重新排列后再继续游戏，根据不同游戏，等待的时间有所不同
			Thread.sleep(1600);
			Dimension screenDimension2 = Toolkit.getDefaultToolkit().getScreenSize();
	        BufferedImage screen2 = robot.createScreenCapture(new Rectangle(screenDimension2));
	        iconAndNumber.clear();
			model=getModel(screen2, squaresInTheRow, squaresInTheColumn, iconAndNumber, screenshotStartPoint);
			oddNumberOfIconExist=false;
			for(Integer numberOfIcon:iconAndNumber.values()) {
				oddNumberOfIconExist|=(numberOfIcon%2!=0);
			}
			if(oddNumberOfIconExist) {
				System.out.println("存在数量为奇数的图标");
				//提醒
				java.awt.Toolkit.getDefaultToolkit().beep();
				return;
			}
			// TODO 宠物连连看有生命限制，应使用[getMoreConnectionStepList]方法
			connectionStepList=getConnectionStepList(model);
        }
        System.out.println("程序执行完毕");
	}
	/**
	 * 连连看经常会出现僵局然后重新排列，这种情况下往往需要重新截图重新识别，而新的界面又往往有非常多的空格，空格数量过多有可能影响
	 * 行列的分割，这时候因为还处于同一局游戏，游戏界面的位置依然相同，所以其实可以截取全屏后复用上一次的分割信息，这时候就需要指定
	 * 截图起点了
	 * 
	 * 传入图片和分割信息，返回模型
	 * @param bimg  传入的图片，如果是截图，则截图起点为0
	 * @param squaresInTheRow  一行的每个格子的起止
	 * @param squaresInTheColumn  一列的每个格子的起止
	 * @param iconAndNumber  统计各种图标的数量，用于判断是否存在奇数个的图标（存在则表示识别出现问题）
	 * @param screenshotStartPoint  上一次截图的起点在当前图像中的位置
	 */
	static int[][] getModel(BufferedImage bimg,List<int[]> squaresInTheRow,List<int[]> squaresInTheColumn,
			Map<Integer,Integer> iconAndNumber,int[] screenshotStartPoint){
		//用于保存不同图案的格子，将同种格子简化为数字
		List<Square> allDifferentSquares=new ArrayList<>();
		//保存识别后的结果
		int[][] model=new int[squaresInTheColumn.size()][squaresInTheRow.size()];
		for(int y=0;y<squaresInTheColumn.size();y++) {
			outer:
			for(int x=0;x<squaresInTheRow.size();x++) {
				//保存当前格子的颜色和数量
				Map<Integer,Integer> colorAndNumber=new HashMap<>();
				//用于保存当前格子有多少不同的像素，如果像素数比较少，可以判断为空格
				Set<Integer> set=new HashSet<>();
				int l=squaresInTheRow.get(x)[0]+screenshotStartPoint[0];
				int r=squaresInTheRow.get(x)[1]+screenshotStartPoint[0];
				int u=squaresInTheColumn.get(y)[0]+screenshotStartPoint[1];
				int d=squaresInTheColumn.get(y)[1]+screenshotStartPoint[1];
				for(int i=l;i<r;i++) {
					for(int j=u;j<d;j++) {
						int rgb=bimg.getRGB(i, j);
						colorAndNumber.compute(rgb, (k,v)->v==null?1:v+1);
						set.add(rgb);
					}
				}
				//这个格子缺少足够的不同像素数，可认为是空格
				if(set.size()<40) {
					model[y][x]=-1;
					continue;
				}
				Square square = new Square(colorAndNumber);
				for(int i=0,size=allDifferentSquares.size();i<size;i++) {
					if(allDifferentSquares.get(i).equals(square)) {
						model[y][x]=i;
						iconAndNumber.compute(i,(k,v)->v+1);
						continue outer;
					}
				}
				allDifferentSquares.add(square);
				model[y][x]=allDifferentSquares.size()-1;
				iconAndNumber.put(allDifferentSquares.size()-1, 1);
			}
		}
		return model;
	}
	/**
	 * 传入识别后的二维数组，返回连接序列
	 * 连接序列中的每个数组存储了（格子1的x,格子1的y,格子2的x,格子2的y）
	 */
	static List<int[]> getConnectionStepList(int[][] model){
		Map<Integer,List<int[]>> iconAndCoordinate=new LinkedHashMap<>();
		//在外层加一圈空格
		int[][] temp=new int[model.length+2][model[0].length+2];
		for(int i=0;i<model.length+2;i++) {
			for(int j=0;j<model[0].length+2;j++) {
				if(i==0||j==0||i==model.length+1||j==model[0].length+1) {
					temp[i][j]=-1;
				}else {
					temp[i][j]=model[i-1][j-1];
					if(model[i-1][j-1]!=-1) {
						//i、j不是final变量无法用于匿名内部类中
						int finalI=i;
						int finalJ=j;
						iconAndCoordinate.compute(model[i-1][j-1], (k,v)->{
							if(v==null) {
								List<int[]> list=new ArrayList<>();
								list.add(new int[] {finalJ,finalI});
								return list;
							}else {
								v.add(new int[] {finalJ,finalI});
								return v;
							}
						});
					}
				}
			}
		}
		List<int[]> connectionStepList=new ArrayList<>();
		while(true) {
			int connectionStepsNumber=connectionStepList.size();
			//遍历每种图标和其坐标集
			iconAndCoordinate.forEach((k,coordinateList)->{
				outer:
				for(int i=0;i<coordinateList.size();i++) {
					int[] coordinate1 = coordinateList.get(i);
					if(temp[coordinate1[1]][coordinate1[0]]==-1) continue;
					Set<String> passedCoordinates=new HashSet<>();
					passedCoordinates.add(coordinate1[0]+","+coordinate1[1]);
					for(int j=i+1;j<coordinateList.size();j++) {
						int[] coordinate2 = coordinateList.get(j);
						if(temp[coordinate2[1]][coordinate2[0]]==-1) continue;
						boolean isAdjoin=Math.abs(coordinate1[0]-coordinate2[0])==1&&coordinate1[1]==coordinate2[1]
								||Math.abs(coordinate1[1]-coordinate2[1])==1&&coordinate1[0]==coordinate2[0];
						if(isAdjoin||canConnect(temp, passedCoordinates, coordinate1, coordinate2, 0, 0, true)) {
							//连接后两个坐标都应该指向-1
							temp[coordinate1[1]][coordinate1[0]]=-1;
							temp[coordinate2[1]][coordinate2[0]]=-1;
							//放到连接序列中的坐标得是原模型的坐标
							connectionStepList.add(new int[] {coordinate1[0]-1,coordinate1[1]-1,coordinate2[0]-1,coordinate2[1]-1});
							// TODO 根据游戏模式修改最后一个参数
							squareMove(temp, iconAndCoordinate, coordinate1, coordinate2, -1);
							//调整后，指向-1的这两个坐标可能会重新指向图标，所以应该将它们修改以绝对指向-1（比如都变成(0,0)坐标）
							coordinate1[0]=coordinate1[1]=coordinate2[0]=coordinate2[1]=0;
							continue outer;
						}
					}
				}
			});
			//没有新增操作，说明要么游戏结束要么没有可连接的项
			if(connectionStepList.size()-connectionStepsNumber==0) break;
		}
		return connectionStepList;
	}
	/**
	 * 传入识别后的二维数组，尽可能返回能够消除最多格子的连接序列
	 * 连接序列中的每个数组存储了（格子1的x,格子1的y,格子2的x,格子2的y）
	 */
	static List<int[]> getMoreConnectionStepList(int[][] model){
		//如果一个连接序列会使游戏陷入僵局（宠物连连看陷入僵局会扣血），则称该连接序列为失败的连接序列
		Set<String> failedConnectionSteps=new LinkedHashSet<>();
		List<int[]> maxConnectionStep=new ArrayList<>();
		List<int[]> minConnectionStep=new ArrayList<>();
		//最短的连接序列的长度不能一开始就是最短的
		for(int i=0;i<100;i++) {
			minConnectionStep.add(null);
		}
		while(true) {
			int numberOfSquaresNotEliminated=0;
			Map<Integer,List<int[]>> iconAndCoordinate=new LinkedHashMap<>();
			//在外层加一圈空格
			int[][] temp=new int[model.length+2][model[0].length+2];
			for(int i=0;i<model.length+2;i++) {
				for(int j=0;j<model[0].length+2;j++) {
					if(i==0||j==0||i==model.length+1||j==model[0].length+1) {
						temp[i][j]=-1;
					}else {
						temp[i][j]=model[i-1][j-1];
						if(model[i-1][j-1]!=-1) {
							numberOfSquaresNotEliminated++;
							//i、j不是final变量无法用于匿名内部类中
							int finalI=i;
							int finalJ=j;
							iconAndCoordinate.compute(model[i-1][j-1], (k,v)->{
								if(v==null) {
									List<int[]> list=new ArrayList<>();
									list.add(new int[] {finalJ,finalI});
									return list;
								}else {
									v.add(new int[] {finalJ,finalI});
									return v;
								}
							});
						}
					}
				}
			}
			List<int[]> connectionStepList=new ArrayList<>();
			StringBuilder connectionStepString=new StringBuilder();
			while(true) {
				int connectionStepsNumber=connectionStepList.size();
				//遍历每种图标和其坐标集
				iconAndCoordinate.forEach((k,coordinateList)->{
					outer:
					for(int i=0;i<coordinateList.size();i++) {
						int[] coordinate1 = coordinateList.get(i);
						if(temp[coordinate1[1]][coordinate1[0]]==-1) continue;
						Set<String> passedCoordinates=new HashSet<>();
						passedCoordinates.add(coordinate1[0]+","+coordinate1[1]);
						for(int j=i+1;j<coordinateList.size();j++) {
							int[] coordinate2 = coordinateList.get(j);
							if(temp[coordinate2[1]][coordinate2[0]]==-1) continue;
							boolean isAdjoin=Math.abs(coordinate1[0]-coordinate2[0])==1&&coordinate1[1]==coordinate2[1]
									||Math.abs(coordinate1[1]-coordinate2[1])==1&&coordinate1[0]==coordinate2[0];
							if((isAdjoin||canConnect(temp, passedCoordinates, coordinate1, coordinate2, 0, 0, true))
									&&!failedConnectionSteps.contains(connectionStepString.toString()
											+"["+(coordinate1[0]-1)+","+(coordinate1[1]-1)+","+(coordinate2[0]-1)+","+(coordinate2[1]-1)+"]")) {
								connectionStepString.append("["+(coordinate1[0]-1)+","+(coordinate1[1]-1)+","+(coordinate2[0]-1)+","+(coordinate2[1]-1)+"]");
								//连接后两个坐标都应该指向-1
								temp[coordinate1[1]][coordinate1[0]]=-1;
								temp[coordinate2[1]][coordinate2[0]]=-1;
								//放到连接序列中的坐标得是原模型的坐标
								connectionStepList.add(new int[] {coordinate1[0]-1,coordinate1[1]-1,coordinate2[0]-1,coordinate2[1]-1});
								// TODO 根据游戏模式修改最后一个参数
								squareMove(temp, iconAndCoordinate, coordinate1, coordinate2, -1);
								//调整后，指向-1的这两个坐标可能会重新指向图标，所以应该将它们修改以绝对指向-1（比如都变成(0,0)坐标）
								coordinate1[0]=coordinate1[1]=coordinate2[0]=coordinate2[1]=0;
								//连接完成后可能会对
								continue outer;
							}
						}
					}
				});
				//没有新增操作，说明要么游戏结束要么没有可连接的项
				if(connectionStepList.size()==connectionStepsNumber) break;
			}
			//如果这是一个失败的连接序列
			if(connectionStepList.size()*2<numberOfSquaresNotEliminated) {
				if(connectionStepList.size()<minConnectionStep.size())
					minConnectionStep=connectionStepList;
				if(connectionStepList.size()>maxConnectionStep.size())
					maxConnectionStep=connectionStepList;
				//如果一个失败的连接序列的连接操作有5个，应当将由其前3个操作、前4个操作、所有操作组成的连接序列都设定为失败，提升效率
				StringBuilder sb=new StringBuilder();
				StringBuilder sb2=new StringBuilder();
				StringBuilder sb3=new StringBuilder();
				StringBuilder sb4=new StringBuilder();
				StringBuilder sb5=new StringBuilder();
				StringBuilder sb6=new StringBuilder();
				//绕过final检查
				int[] num=new int[1];
				connectionStepList.forEach(arr->{
					num[0]++;
					sb.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					if(num[0]<connectionStepList.size()) {//略去最后1对连接
						sb2.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					}
					if(num[0]<connectionStepList.size()-1) {//略去最后2对连接
						sb3.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					}
					if(num[0]<connectionStepList.size()-2) {//略去最后3对连接
						sb4.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					}
					if(num[0]<connectionStepList.size()-3) {//略去最后4对连接
						sb5.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					}
					if(num[0]<connectionStepList.size()-4) {//略去最后5对连接
						sb6.append("["+arr[0]+","+arr[1]+","+arr[2]+","+arr[3]+"]");
					}
				});
				boolean b=failedConnectionSteps.add(sb.toString());
				failedConnectionSteps.add(sb2.toString());
				failedConnectionSteps.add(sb3.toString());
				failedConnectionSteps.add(sb4.toString());
				failedConnectionSteps.add(sb5.toString());
				failedConnectionSteps.add(sb6.toString());
				//用于调试
//				System.out.println("失败的连接序列有"+failedConnectionSteps.size()+"个，最长的连接序列的长度"+maxConnectionStep.size()
//					+" 最短的连接序列的长度"+minConnectionStep.size());
				//如果已经没有新的连接序列或已经有2000种失败的连接序列，则不再追求完全消除
				if(!b||failedConnectionSteps.size()>2000) {
//					failedConnectionSteps.forEach(System.out::println);
					System.out.println("一共有"+failedConnectionSteps.size()+"种连接序列");
					System.out.println("最长的连接序列的长度"+maxConnectionStep.size());
					System.out.println("最短的连接序列的长度"+minConnectionStep.size());
//					System.exit(0);
					return maxConnectionStep;
				}
			}else{
				return connectionStepList;
			};
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整<br>
	 * 调整模式：<br>
	 * -1：不调整<br>
	 * 0：不知道具体模式，走一步看一步<br>
	 * 1：上下分离<br>
	 * 2：左右分离<br>
	 * 3：从右向左<br>
	 * 4：从上到下<br>
	 * 5：从下到上<br>
	 * 6：从左到右<br>
	 * 7：中心聚集<br>
	 */
	static void squareMove(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare1,int[] eliminatedSquare2,int moveMode) {
		switch(moveMode) {
		case -1:
			break;
		case 0:
			squareMove0(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove0(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 1:
			squareMove1(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove1(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 2:
			squareMove2(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove2(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 3:
			squareMove3(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove3(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 4:
			squareMove4(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove4(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 5:
			squareMove5(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove5(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 6:
			squareMove6(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove6(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		case 7:
			squareMove7(newModel, iconAndCoordinate, eliminatedSquare1);
			squareMove7(newModel, iconAndCoordinate, eliminatedSquare2);
			break;
		default:
			throw new RuntimeException("没有这种调整模式");
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：未知，不再继续走下去，置空所有格子，重新识别图像
	 */
	static void squareMove0(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		for(int i=0;i<newModel.length;i++) {
			for(int j=0;j<newModel[i].length;j++) {
				newModel[i][j]=-1;
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从中间行向上下挪
	 */
	static void squareMove1(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfRowNotNeedMove1=0,indexOfRowNotNeedMove2=0;
		if(newModel.length%2==0) {
			indexOfRowNotNeedMove2=newModel.length/2;
			indexOfRowNotNeedMove1=indexOfRowNotNeedMove2-1;
		}else {
			indexOfRowNotNeedMove1=indexOfRowNotNeedMove2=newModel.length/2;
		}
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[1]==indexOfRowNotNeedMove1||eliminatedSquare[1]==indexOfRowNotNeedMove2) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有纵向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[1][eliminatedSquare[0]]==-1;
			//从中间向两边遍历模型的指定列，注意新模型周围一圈的空格不能放图标
			for(int y=2;y<=indexOfRowNotNeedMove1;y++) {
				if(newModel[y][eliminatedSquare[0]]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[y][eliminatedSquare[0]];
					int finalY=y;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==eliminatedSquare[0]&&index[1]==finalY) {
							index[1]=finalY-1;
						}
					});
					newModel[y-1][eliminatedSquare[0]]=iconToMove;
					newModel[y][eliminatedSquare[0]]=-1;
				}
			}
			previousSquareEmpty=newModel[newModel.length-2][eliminatedSquare[0]]==-1;
			for(int y=newModel.length-3;y>=indexOfRowNotNeedMove2;y--) {
				if(newModel[y][eliminatedSquare[0]]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[y][eliminatedSquare[0]];
					int finalY=y;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==eliminatedSquare[0]&&index[1]==finalY) {
							index[1]=finalY+1;
						}
					});
					newModel[y+1][eliminatedSquare[0]]=iconToMove;
					newModel[y][eliminatedSquare[0]]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从中间列向左右挪
	 */
	static void squareMove2(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfColumnNotNeedMove1=0,indexOfColumnNotNeedMove2=0;
		if(newModel[0].length%2==0) {
			indexOfColumnNotNeedMove2=newModel[0].length/2;
			indexOfColumnNotNeedMove1=indexOfColumnNotNeedMove2-1;
		}else {
			indexOfColumnNotNeedMove1=indexOfColumnNotNeedMove2=newModel[0].length/2;
		}
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[0]==indexOfColumnNotNeedMove1||eliminatedSquare[0]==indexOfColumnNotNeedMove2) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有横向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[eliminatedSquare[1]][1]==-1;
			//从中间向两边遍历模型的指定行，注意新模型周围一圈的空格不能放图标
			for(int x=2;x<=indexOfColumnNotNeedMove1;x++) {
				if(newModel[eliminatedSquare[1]][x]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[eliminatedSquare[1]][x];
					int finalX=x;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==finalX&&index[1]==eliminatedSquare[1]) {
							index[0]=finalX-1;
						}
					});
					newModel[eliminatedSquare[1]][x-1]=iconToMove;
					newModel[eliminatedSquare[1]][x]=-1;
				}
			}
			previousSquareEmpty=newModel[eliminatedSquare[1]][newModel[0].length-2]==-1;
			for(int x=newModel[0].length-3;x>=indexOfColumnNotNeedMove2;x--) {
				if(newModel[eliminatedSquare[1]][x]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[eliminatedSquare[1]][x];
					int finalX=x;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==finalX&&index[1]==eliminatedSquare[1]) {
							index[0]=finalX+1;
						}
					});
					newModel[eliminatedSquare[1]][x+1]=iconToMove;
					newModel[eliminatedSquare[1]][x]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从最右列向最左列挪
	 */
	static void squareMove3(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfColumnNotNeedMove=newModel[0].length-2;
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[0]==indexOfColumnNotNeedMove) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有横向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[eliminatedSquare[1]][1]==-1;
			//从中间向两边遍历模型的指定行，注意新模型周围一圈的空格不能放图标
			for(int x=2;x<=indexOfColumnNotNeedMove;x++) {
				if(newModel[eliminatedSquare[1]][x]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[eliminatedSquare[1]][x];
					int finalX=x;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==finalX&&index[1]==eliminatedSquare[1]) {
							index[0]=finalX-1;
						}
					});
					newModel[eliminatedSquare[1]][x-1]=iconToMove;
					newModel[eliminatedSquare[1]][x]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从第一行向最后行挪
	 */
	static void squareMove4(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfRowNotNeedMove=1;
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[1]==indexOfRowNotNeedMove) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有纵向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[newModel.length-2][eliminatedSquare[0]]==-1;
			//从中间向两边遍历模型的指定列，注意新模型周围一圈的空格不能放图标
			for(int y=newModel.length-3;y>=indexOfRowNotNeedMove;y--) {
				if(newModel[y][eliminatedSquare[0]]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[y][eliminatedSquare[0]];
					int finalY=y;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==eliminatedSquare[0]&&index[1]==finalY) {
							index[1]=finalY+1;
						}
					});
					newModel[y+1][eliminatedSquare[0]]=iconToMove;
					newModel[y][eliminatedSquare[0]]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从最后一行向第一行挪
	 */
	static void squareMove5(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfRowNotNeedMove=newModel.length-2;
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[1]==indexOfRowNotNeedMove) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有纵向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[1][eliminatedSquare[0]]==-1;
			//从中间向两边遍历模型的指定列，注意新模型周围一圈的空格不能放图标
			for(int y=2;y<=indexOfRowNotNeedMove;y++) {
				if(newModel[y][eliminatedSquare[0]]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[y][eliminatedSquare[0]];
					int finalY=y;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==eliminatedSquare[0]&&index[1]==finalY) {
							index[1]=finalY-1;
						}
					});
					newModel[y-1][eliminatedSquare[0]]=iconToMove;
					newModel[y][eliminatedSquare[0]]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：从最左列向最右列挪
	 */
	static void squareMove6(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		int indexOfColumnNotNeedMove=1;
		//被消除的格子处于不用移动的行上，不会影响其他格子，不需要调整
		if(eliminatedSquare[0]==indexOfColumnNotNeedMove) return;
		for(int i=0;i<2;i++) {//这一层循环是为了防止有横向连续两个空格但只挪动一格的情况
			boolean previousSquareEmpty=newModel[eliminatedSquare[1]][newModel[0].length-2]==-1;
			//从中间向两边遍历模型的指定行，注意新模型周围一圈的空格不能放图标
			for(int x=newModel[0].length-3;x>=indexOfColumnNotNeedMove;x--) {
				if(newModel[eliminatedSquare[1]][x]==-1) {
					previousSquareEmpty=true;
					continue;
				}
				if(previousSquareEmpty) {
					int iconToMove=newModel[eliminatedSquare[1]][x];
					int finalX=x;
					iconAndCoordinate.get(iconToMove).forEach(index->{
						if(index[0]==finalX&&index[1]==eliminatedSquare[1]) {
							index[0]=finalX+1;
						}
					});
					newModel[eliminatedSquare[1]][x+1]=iconToMove;
					newModel[eliminatedSquare[1]][x]=-1;
				}
			}
		}
	}
	/**
	 * 消除一对格子之后就要对模型和图标的位置序列进行调整
	 * 调整模式：向中间靠拢，具体算法看不出来，但一个格子的消除只会导致其向外矩形区域的改变，被改变的区域会变成什么样无法得知，
	 * 因此该区域在重新识别之前不应再被访问，注意不能直接将该区域置空，因为空是可以连线的，而它们只是变为未知，不是空，不能连线
	 */
	static void squareMove7(int[][] newModel,Map<Integer,List<int[]>> iconAndCoordinate,int[] eliminatedSquare) {
		//被消除的坐标会被置空，这些空很可能会被临近图标占领，但本算法并不挪动，所以得把被消除的坐标填充回去
		if(eliminatedSquare[0]!=0&&eliminatedSquare[1]!=0)//注意坐标1可能会将坐标2变为0,0，这种情况下就不需要操作了
			newModel[eliminatedSquare[1]][eliminatedSquare[0]]=1;
		List<int[]> indexOfSquaresNotNeedMove=new ArrayList<>();
		indexOfSquaresNotNeedMove.add(new int[] {1,1});
		indexOfSquaresNotNeedMove.add(new int[] {1,newModel.length-2});
		indexOfSquaresNotNeedMove.add(new int[] {newModel[0].length-2,1});
		indexOfSquaresNotNeedMove.add(new int[] {newModel[0].length-2,newModel.length-2});
		for(int i=0;i<4;i++) {
			if(eliminatedSquare[0]==indexOfSquaresNotNeedMove.get(i)[0]&&eliminatedSquare[1]==indexOfSquaresNotNeedMove.get(i)[1])
				return;
		}
		//标记要屏蔽的区域
		int x1=0,x2=0,y1=0,y2=0;
		if(eliminatedSquare[0]<=newModel[0].length/2-1) {
			if(eliminatedSquare[1]<=newModel.length/2-1) {
				x1=1;x2=eliminatedSquare[0];
				y1=1;y2=eliminatedSquare[1];
			}else {
				x1=1;x2=eliminatedSquare[0];
				y1=eliminatedSquare[1];y2=newModel.length-2;
			}
		}else {
			if(eliminatedSquare[1]<=newModel.length/2-1) {
				x1=eliminatedSquare[0];x2=newModel[0].length-2;
				y1=1;y2=eliminatedSquare[1];
			}else {
				x1=eliminatedSquare[0];x2=newModel[0].length-2;
				y1=eliminatedSquare[1];y2=newModel.length-2;
			}
		}
		int finalX1=x1,finalX2=x2,finalY1=y1,finalY2=y2;
		//所有指向屏蔽区域的坐标都要指向到空格上（比如指向(0,0)）
		iconAndCoordinate.forEach((k,coordinateList)->{
			for(int i=0;i<coordinateList.size();i++) {
				int[] index = coordinateList.get(i);
				if(index[0]>=finalX1&&index[0]<=finalX2&&index[1]>=finalY1&&index[1]<=finalY2) {
					index[0]=0;
					index[1]=0;
				}
			}
		});
	}
	/**
	 * 判断模型中的两个坐标能否连接，会被递归调用
	 * [被走过的坐标]就像父亲对儿子们的教诲，叫他们不要走父亲的老路，而每个儿子又会教诲他们的儿子不要走你爷爷+你爹的老路
	 * A - - X B
	 * | X | - |
	 * | X X X |
	 * | X X X |
	 * - - - - |
	 * 上次的方向表示坐标1相对于其上一个坐标的方位，如果坐标1是第一个坐标，则方向是0
	 */
	static boolean canConnect(int[][] newModel,Set<String> passedCoordinates,int[] coordinate1,int[] coordinate2,
			int lastDirection,int turnTimes,boolean coordinate1IsOriginalStartPoint) {
		if(turnTimes>=3) {
			return false;
		}
		//坐标1就是父亲，被走过的坐标就是父亲走过的路，合格的儿子不能站在父亲走过的路上
		List<int[]> canMoveToCoordinates = getCanMoveToCoordinates(newModel, passedCoordinates, coordinate1,coordinate1IsOriginalStartPoint);
//		System.out.print(Arrays.toString(coordinate1)+":");
//		canMoveToCoordinates.forEach(arr->System.out.print(Arrays.toString(arr)));
//		System.out.println();
		for(int i=0;i<canMoveToCoordinates.size();i++) {
			int[] arr=canMoveToCoordinates.get(i);
			if(turnTimes+(lastDirection==0||arr[2]==lastDirection?0:1)<3&&arr[0]==coordinate2[0]&&arr[1]==coordinate2[1]) {
//				System.out.println(Arrays.toString(arr)+"转弯次数"+(turnTimes+(lastDirection==0||arr[2]==lastDirection?0:1)));
//				System.out.println();
				return true;
			}
		}
		boolean result=false;
		for(int i=0;i<canMoveToCoordinates.size();i++) {
			int[] arr=canMoveToCoordinates.get(i);
			Set<String> passedCoordinates2=new HashSet<>(passedCoordinates);
			passedCoordinates2.add(arr[0]+","+arr[1]);
			result|=canConnect(newModel, passedCoordinates2, arr, coordinate2,arr[2],turnTimes+(lastDirection==0||arr[2]==lastDirection?0:1),false);
			if(result) return true;
//			System.out.print(Arrays.toString(arr)+"over 共转弯"+(turnTimes+(lastDirection==0||arr[2]==lastDirection?0:1)));
//			System.out.println();
		}
		return result;
	}
	/**
	 * 返回一个坐标周围的可走坐标和该坐标相对于指定坐标的方位，1,2,3,4分别代表上下左右
	 * 原始起点只能走向空格，空格可以走向空格和非空格，除了原始起点外，非空格不能继续走
	 */
	static List<int[]> getCanMoveToCoordinates(int[][] newModel,Set<String> passedCoordinates,int[] index,boolean coordinateIsOriginalStartPoint){
		List<int[]> result=new ArrayList<>();
		//每个格子都有4个方向
		result.add(new int[]{index[0],index[1]-1,1});
		result.add(new int[]{index[0],index[1]+1,2});
		result.add(new int[]{index[0]-1,index[1],3});
		result.add(new int[]{index[0]+1,index[1],4});
		ListIterator<int[]> listIterator = result.listIterator();
		while(listIterator.hasNext()) {
			int[] next = listIterator.next();
			//可走的坐标必须是合法坐标
			if(next[0]>=0&&next[0]<newModel[0].length&&next[1]>=0&&next[1]<newModel.length
					//要么起点是原始起点且目标格是空格，要么起点是空格
					&&(newModel[next[1]][next[0]]==-1&&coordinateIsOriginalStartPoint||newModel[index[1]][index[0]]==-1)
					//不走已经走过的路
					&&!passedCoordinates.contains(next[0]+","+next[1])) {
				continue;
			}
			listIterator.remove();
		}
		return result;
	}
	/**
	 * 保存一个格子的标识信息，用于比较两个格子是否相同
	 */
	static class Square{
		//格子的所有颜色和对应数量
		List<int[]> colorAndNumber=new ArrayList<>();
		//格子的总像素数
		int totalPixelNumber=0;
		int[] rgbAverageValue= {0,0,0};
		public Square(Map<Integer,Integer> colorNumber) {
			colorNumber.forEach((k,v)->{
				int colorR = ((k&0xff0000)>>16);
				int colorG = ((k&0xff00)>>8);
				int colorB = k&0xff;
				totalPixelNumber+=v;
				colorAndNumber.add(new int[] {colorR,colorG,colorB,v});
			});
			int[] colorSumAndPixelNumberOfCurrentSquare= {0,0,0,0};
			colorAndNumber.forEach(arr->{
				colorSumAndPixelNumberOfCurrentSquare[0]+=(arr[0]*arr[3]);
				colorSumAndPixelNumberOfCurrentSquare[1]+=(arr[1]*arr[3]);
				colorSumAndPixelNumberOfCurrentSquare[2]+=(arr[2]*arr[3]);
				colorSumAndPixelNumberOfCurrentSquare[3]+=arr[3];
			});
			rgbAverageValue[0]=colorSumAndPixelNumberOfCurrentSquare[0]/colorSumAndPixelNumberOfCurrentSquare[3];
			rgbAverageValue[1]=colorSumAndPixelNumberOfCurrentSquare[1]/colorSumAndPixelNumberOfCurrentSquare[3];
			rgbAverageValue[2]=colorSumAndPixelNumberOfCurrentSquare[2]/colorSumAndPixelNumberOfCurrentSquare[3];
		}
		@Override
		public boolean equals(Object obj) {
			Square gz=(Square)obj;
			//如果两个格子的rgb均值高度匹配，可认为它们相同
			if(Math.abs(rgbAverageValue[0]-gz.rgbAverageValue[0])<2&&Math.abs(rgbAverageValue[1]-gz.rgbAverageValue[1])<2
					&&Math.abs(rgbAverageValue[2]-gz.rgbAverageValue[2])<2) {
				return true;
			}
			//尽早结束不同图标的比较可以提升速度
			//如果两个格子的rgb均值高度不匹配，可认为它们必然不相同
			if(Math.abs(rgbAverageValue[0]-gz.rgbAverageValue[0])>20||Math.abs(rgbAverageValue[1]-gz.rgbAverageValue[1])>20
					||Math.abs(rgbAverageValue[2]-gz.rgbAverageValue[2])>20) {
				return false;
			}
			
			List<int[]> colorList1=new ArrayList<>();
			List<int[]> colorList2=new ArrayList<>();
			colorAndNumber.forEach(arr->colorList1.add(new int[] {arr[0],arr[1],arr[2],arr[3]}));
			gz.colorAndNumber.forEach(arr->colorList2.add(new int[] {arr[0],arr[1],arr[2],arr[3]}));
			int numberOfPixelsOfExactlyMatchColor=0;
			int numberOfPixelsOfRoughlyMatchColor=0;
			int matchNumberOfRareColor=0;
			int totalPixelsNumberOfTwoSquares=totalPixelNumber+gz.totalPixelNumber;
			long startTimeOfIconComparison = System.currentTimeMillis();
			//对于格子1中的所有颜色，都先去格子2中找完全一样的共同减去，再去格子2中找类似的共同减去
			for(int allowableOffset=1;allowableOffset<30;allowableOffset++) {
				for(int i=0;i<colorList1.size();i++) {
					int[] rgb=colorList1.get(i);
					//格子1中最多的几种颜色和对应的数量如果在格子2中也都存在，则两个格子的图案相同
					for(int j=0;rgb[3]>0&&j<colorList2.size();j++) {
						int[] rgb2=colorList2.get(j);
						if(rgb2[3]==0) continue;
						if(Math.abs(rgb[0]-rgb2[0])<allowableOffset&&Math.abs(rgb[1]-rgb2[1])<allowableOffset
								&&Math.abs(rgb[2]-rgb2[2])<allowableOffset) {
							int togetherSubValue=Math.min(rgb[3], rgb2[3]);
							rgb[3]-=togetherSubValue;
							rgb2[3]-=togetherSubValue;
							numberOfPixelsOfRoughlyMatchColor+=togetherSubValue;
							//数量少于10的颜色是最广泛存在的，相同图标在比较时能及时把这些颜色的数量置为0以减少循环次数
							if(togetherSubValue<10) matchNumberOfRareColor+=togetherSubValue;
						}
					}
				}
				/*
				 * 相同图标在比较时会尽快将细碎的颜色置0（虽然细碎但确实相同），而不相同的图标则无法快速地将细碎的颜色去除，
				 * 但当允许的偏差变大时也会一点一点的将这些细碎颜色置0，果蔬连连看的细碎颜色在不同图标中偏差很大，使循环比较
				 * 不会关注于它们，而宠物连连看的细碎颜色偏差不大，所以循环过程中总有细碎颜色满足偏差条件所以不得不处理，所以
				 * 优化思路为：如果少数颜色不能尽早地被大量处理，则认为两个图标不相同，当然，会有一些不相同图片在比较时也能快速
				 * 大量消除细碎颜色，这种是少数可以忽略，也有一些相同图片无法尽早消除细碎颜色，因此消除细碎颜色的量不能设定太大‘
				 * 
				 * 果蔬连连看的不相同图标自然也是不会快速消除细碎颜色的，但它也不会在后序循环蹦出来，可以理解为直到最后它也不会
				 * 去消除这些细碎颜色，而宠物连连看就会，这就是宠物连连看在比较不同图标时慢的原因
				 * 宠物连连看的不同图标在比较时平均会处理占总像素数29%的细碎颜色（数量少于50的颜色）
				 * 果蔬连连看的不同图标在比较时平均会处理占总像素数15%的细碎颜色（数量少于50的颜色）
				 */
				//在精确匹配时只能处理掉不到4%的细碎颜色（数量少于10的颜色），可认为是不相同的图标
				// TODO 宠物连连看使用0.26，果蔬连连看使用0.03
				if(allowableOffset==1&&(matchNumberOfRareColor*2.0/totalPixelsNumberOfTwoSquares)<0.03) {
					return false;
				}
				/*
				 * 宠物连连看的不同图标也常常有很大比例的完全一样的像素
				 * 如果偏差小于5即为完全匹配
				 * 果蔬连连看
				 * 图标识别完成，耗时17秒
				 * 相似图标在比较时的平均耗时为2毫秒
				 * 不相似图标在比较时的平均耗时为7毫秒
				 * 相似图标的最大完全匹配度0.9988073941562313
				 * 相似图标的最小完全匹配度0.1810344827586207
				 * 相似图标的平均完全匹配度0.5703080967299325
				 * 不相似图标的最大完全匹配度0.632996632996633
				 * 不相似图标的最小完全匹配度0.0
				 * 不相似图标的平均完全匹配度0.11048238772312945
				 * 宠物连连看
				 * 图标识别完成，耗时105秒
				 * 相似图标在比较时的平均耗时为1毫秒
				 * 不相似图标在比较时的平均耗时为59毫秒
				 * 相似图标的最大完全匹配度1.0
				 * 相似图标的最小完全匹配度0.8673550436854647
				 * 相似图标的平均完全匹配度0.9347089799831346
				 * 不相似图标的最大完全匹配度0.7014846235418876
				 * 不相似图标的最小完全匹配度0.01713520749665328
				 * 不相似图标的平均完全匹配度0.31377839115254985
				 * 
				 * 可见如果果蔬连连看的两个图标相同，它们在偏差值到5之前平均也只有57%的相似度
				 * 而宠物连连看的两个相同图标在偏差值到5之前就能达到93%的平均相似度，所以宠物连连看的相同图标
				 * 能尽早地将颜色集合中的许多颜色数量减到0，使得后续循环的计算量下降，从而让相似图标的比较快于
				 * 果蔬连连看，当然，因为相似图标的颜色集合中的颜色数量总是一直在减少的，所以后序循环的计算量并
				 * 不会太大，主要计算量还是在于不相同图标比较上，不相同图标的比较的计算量大是因为既不能把很多颜色
				 * 数量都减为0（减为0可以免去很多循环），又确实有很多颜色相似所以总是在进行减少数量的操作（主要都是减1）
				 * ，所以可以看见宠物连连看在比较不相同图标时耗时很长，这是因为宠物练练看的不同图标的相似度比较高，平均
				 * 相似度差不多是果蔬连连看的3倍，所以宠物练练看的不同图标的比较就总是在进行[即不把很多颜色的数量
				 * 减为0，又总是在一点一点的减少着]的操作
				 */
			}
			
			double similarityDegree=numberOfPixelsOfRoughlyMatchColor*2.0/totalPixelsNumberOfTwoSquares;
			long endTimeOfIconComparison = System.currentTimeMillis();
			if(similarityDegree>0.9) {
				comparisonTimesOfSimilarIcons++;
				totalTimeSpentComparingSimilarIcons+=(endTimeOfIconComparison-startTimeOfIconComparison);
				maxMatchDegreeOfSimilarIcons=Math.max(maxMatchDegreeOfSimilarIcons, numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares);
				minMatchDegreeOfSimilarIcons=Math.min(minMatchDegreeOfSimilarIcons, numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares);
				totalMatchDegreeOfSimilarIcons+=numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares;
			}else {
				comparisonTimesOfDifferentIcons++;
				totalTimeSpentComparingDifferentIcons+=(endTimeOfIconComparison-startTimeOfIconComparison);
				maxMatchDegreeOfDifferentIcons=Math.max(maxMatchDegreeOfDifferentIcons, numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares);
				minMatchDegreeOfDifferentIcons=Math.min(minMatchDegreeOfDifferentIcons, numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares);
				totalMatchDegreeOfDifferentIcons+=numberOfPixelsOfExactlyMatchColor*2.0/totalPixelsNumberOfTwoSquares;
			}
			//用于调试
//			System.out.println("相似度："+similarityDegree);
			return similarityDegree>0.9;
		}
	}
	//用于调试
	static int comparisonTimesOfSimilarIcons=0;
	static long totalTimeSpentComparingSimilarIcons=0L;
	static int comparisonTimesOfDifferentIcons=0;
	static long totalTimeSpentComparingDifferentIcons=0L;
	static double maxMatchDegreeOfSimilarIcons=0;
	static double minMatchDegreeOfSimilarIcons=1;
	static double totalMatchDegreeOfSimilarIcons=0;
	static double maxMatchDegreeOfDifferentIcons=0;
	static double minMatchDegreeOfDifferentIcons=1;
	static double totalMatchDegreeOfDifferentIcons=0;
	/**
	 * 显示界面的具体切割
	 */
	static void show(BufferedImage bimg,List<int[]> squaresInTheRow,List<int[]> squaresInTheColumn) {
		Graphics g=bimg.getGraphics();
		g.setColor(Color.MAGENTA);
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(3.0f));
		Set<Integer> widths=new HashSet<>();
		Set<Integer> heights=new HashSet<>();
		int num=0;
		for(int x=0;x<squaresInTheRow.size();x++) {
			for(int y=0;y<squaresInTheColumn.size();y++) {
				int l=squaresInTheRow.get(x)[0];
				int r=squaresInTheRow.get(x)[1];
				int u=squaresInTheColumn.get(y)[0];
				int d=squaresInTheColumn.get(y)[1];
				num++;
				g.drawRect(l, u, r-l, d-u);
				widths.add(r-l);
				heights.add(d-u);
			}
		}
		System.out.println("框选了"+num+"个格子");
		System.out.println("宽的种类（单位像素）："+widths);
		System.out.println("高的种类（单位像素）："+heights);
		JFrame f = new JFrame();
		JPanel p = new JPanel() {
			@Override
			public void paint(Graphics g) {
				g.drawImage(bimg, 0, 0, null);
			}
		};
		p.setPreferredSize(new Dimension(bimg.getWidth(), bimg.getHeight()));
		f.add(p);
		f.pack();
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	/**
	 * 获取横向每一格的起点和终点
	 */
	static List<int[]> getSquaresStartAndEndPointsInTheRow(BufferedImage bimg,List<Integer> PixelList) {
		List<int[]> squaresStartAndEndPointsInTheRow=new ArrayList<>();
		int previousSplitLineAbscissa=0;
		for(int i=0,len=PixelList.size();i<len;i++) {
			if(isSplitLine(getTheColumnPixelList(bimg, i))) {
				//如果当前分割线与上一条分割线的距离超过20像素，则认为两条分割线界定出一个格子
				if(i-previousSplitLineAbscissa>20) {
					squaresStartAndEndPointsInTheRow.add(new int[] {previousSplitLineAbscissa+1,i-1});
				}
				previousSplitLineAbscissa=i;
			}
		}
		return squaresStartAndEndPointsInTheRow;
	}
	/**
	 * 获取纵向每一格的起点和终点
	 */
	static List<int[]> getSquaresStartAndEndPointsInTheColumn(BufferedImage bimg,List<Integer> PixelList) {
		List<int[]> squaresStartAndEndPointsInTheColumn=new ArrayList<>();
		int previousSplitLineOrdinate=0;
		for(int i=0,len=PixelList.size();i<len;i++) {
			if(isSplitLine(getTheRowPixelList(bimg, i))) {
				//如果当前分割线与上一条分割线的距离超过20像素，则有理由相信两条分割线界定出一个格子
				if(i-previousSplitLineOrdinate>20) {
					squaresStartAndEndPointsInTheColumn.add(new int[] {previousSplitLineOrdinate+1,i-1});
				}
				previousSplitLineOrdinate=i;
			}
		}
		if(squaresStartAndEndPointsInTheColumn.size()>1) return squaresStartAndEndPointsInTheColumn;
		//石头连连看的横向分割线被纵向分割线切割导致无法被识别，需要更精确的方法
		squaresStartAndEndPointsInTheColumn.clear();
		previousSplitLineOrdinate=0;
		for(int i=0,len=PixelList.size();i<len;i++) {
			if(isSplitLine2(getTheRowPixelList(bimg, i))) {
				//如果当前分割线与上一条分割线的距离超过20像素，则有理由相信两条分割线界定出一个格子
				if(i-previousSplitLineOrdinate>20) {
					squaresStartAndEndPointsInTheColumn.add(new int[] {previousSplitLineOrdinate+1,i-1});
				}
				previousSplitLineOrdinate=i;
			}
		}
		return squaresStartAndEndPointsInTheColumn;
	}
	
	
	/**
	 * 判断给定像素集是否是分割线，有理由相信一条分割线的中间80%的颜色是近似的
	 */
	static boolean isSplitLine(List<Integer> PixelList) {
		int allowableErrorValue=126;
		int l=(int)(PixelList.size()*0.1);
		int r=(int)(PixelList.size()*0.9);
		int[] rarr=new int[r-l];
		int[] garr=new int[r-l];
		int[] barr=new int[r-l];
		for(int i=0;i<rarr.length;i++) {
			int rgb=PixelList.get(l+i);
			rarr[i]=((rgb&0xff0000)>>16);
			garr[i]=((rgb&0xff00)>>8);
			barr[i]=rgb&0xff;
		}
		Arrays.parallelSort(rarr);
		Arrays.parallelSort(garr);
		Arrays.parallelSort(barr);
		//如果r、g、b三种值的最大和最小都不超过60，则可认为颜色一致
		if(rarr[r-l-1]-rarr[0]<allowableErrorValue&&garr[r-l-1]-garr[0]<allowableErrorValue
				&&barr[r-l-1]-barr[0]<allowableErrorValue) {
			return true;
		}
		return false;
	}
	/**
	 * 判断给定像素集是否是分割线，更精确
	 */
	static boolean isSplitLine2(List<Integer> PixelList) {
		Map<Integer,Integer> colorAndNumber=new HashMap<>();
		Map<Integer,List<Integer>> colorAndIndex=new HashMap<>();
		int l=(int)(PixelList.size()*0.1);
		int r=(int)(PixelList.size()*0.9);
		int[] rarr=new int[r-l];
		int[] garr=new int[r-l];
		int[] barr=new int[r-l];
		for(int i=0;i<rarr.length;i++) {
			int rgb=PixelList.get(l+i);
			rarr[i]=((rgb&0xff0000)>>16);
			garr[i]=((rgb&0xff00)>>8);
			barr[i]=rgb&0xff;
			colorAndNumber.compute(rgb, (k,v)->v==null?1:v+1);
			int finalI=i;
			colorAndIndex.compute(rgb, (k,v)->{
				if(v==null) {
					List<Integer> list=new ArrayList<>();
					list.add(finalI);
					return list;
				}else {
					v.add(finalI);
					return v;
				}
			});
		}
		int[] mostSpecialColorAndItsNumber=new int[2];
		mostSpecialColorAndItsNumber[1]=Integer.MAX_VALUE;
		colorAndNumber.forEach((rgb,v)->{
			if(v<mostSpecialColorAndItsNumber[1]) {
				mostSpecialColorAndItsNumber[0]=rgb;
				mostSpecialColorAndItsNumber[1]=v;
			}
		});
		List<Integer> mostSpecialColorIndex=colorAndIndex.get(mostSpecialColorAndItsNumber[0]);
		//只有一个特殊点，不是分割线
		if(mostSpecialColorIndex.size()<2) return false;
		int distanceBetweenSpecialColors=mostSpecialColorIndex.get(1)-mostSpecialColorIndex.get(0);
		//特殊点的距离应该是基本等距的
		for(int i=2;i<mostSpecialColorIndex.size();i++) {
			if(Math.abs(mostSpecialColorIndex.get(i)-mostSpecialColorIndex.get(i-1)-distanceBetweenSpecialColors)>4) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 获得给定X坐标所在列的像素集
	 */
	static List<Integer> getTheColumnPixelList(BufferedImage bimg,int X){
		return getPixelsOfRowOrColumn(bimg, X, true);
	}
	/**
	 * 获得给定Y坐标所在行的像素集
	 */
	static List<Integer> getTheRowPixelList(BufferedImage bimg,int Y){
		return getPixelsOfRowOrColumn(bimg, Y, false);
	}
	/**
	 * 辅助方法
	 */
	private static List<Integer> getPixelsOfRowOrColumn(BufferedImage bimg,int index,boolean isX){
		List<Integer> PixelList=new ArrayList<>();
		for(int i=0,max=isX?bimg.getHeight():bimg.getWidth();i<max;i++) {
			int rgb = isX?bimg.getRGB(index, i):bimg.getRGB(i, index);
			PixelList.add(rgb);
		}
		return PixelList;
	}
}
