import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Random;

class GameInfo
{
	static HexBlock[][] allVisibleBlock;
	
	static Color dragColor;
	
	static LinkedList<HexBlock> dragBlock;
	
	static LinkedList<HexBlock> tempList;
	
	static Hex bigHex;
	
	static boolean available(int r, int c)
	{
		return (r >= 0 && r < 9 && c >= 0 && c < 17 && GameInfo.allVisibleBlock[r][c].getAvailable() && GameInfo.allVisibleBlock[r][c].getFillState() == false);
	}
}

class MyPolygon extends Polygon
{
	private int row, column, n;
	
	public int getRow()
	{
		return row;
	}
	
	public void setRow(int row)
	{
		this.row = row;
	}
	
	public int getColumn()
	{
		return column;
	}
	
	public void setColumn(int column)
	{
		this.column = column;
	}
	
	public void setN(int n)
	{
		this.n = n;
	}
	
	public int getN()
	{
		return n;
	}
	
}

//六边形块
class HexBlock {
	//颜色
	private Color color;
	private boolean isAvailable;
	private boolean isFilled;
	private MyPolygon p;
	private double centerX;
	private double centerY;
	private double length;
	private static Pane drawPane;
	
	//构造函数
	public HexBlock(boolean available) {
		isAvailable = available;
		color = Color.rgb(77, 77, 75);
		isFilled = false;
	}

	//获取可用性
	public boolean getAvailable() {
		return isAvailable;
	}

	//设置可用性
	public void setAvailable(boolean available) {
		isAvailable = available;
	}
	
	//获取填充状态
	public boolean getFillState()
	{
		return isFilled;
	}
	
	public void setFillState(boolean fillState)
	{
		isFilled = fillState;
	}
	
	//设定块的颜色
	public boolean setColor(int r, int g, int b, double o)
	{
		if(isAvailable)
		{
			color = Color.rgb(r, g, b, o);
			if(p != null)
			{
				p.setFill(color);
				p.setStroke(color);
			}
			
			return true;
		}
		return false;
	}
	
	public boolean setColor(Color c)
	{
		color = c;
		return true;
	}
	
	public boolean setColor(int r, int g, int b)
	{
		return setColor(r, g, b, 1);
	}
	
	//取得颜色
	public Color getColor()
	{
		if(isAvailable)
			return color;
		return null;
	}
	
	public void setShadow()
	{
		DropShadow ds = new DropShadow();
        ds.setOffsetY(5.0);
        ds.setOffsetX(5.0);
		p.setEffect(ds);
	}
	
	//设置六边形参数
	public void setHex(double x, double y, double l)
	{
		centerX = x;
		centerY = y;
		length = l;
	}
	
	//设置要绘制的pane
	public void setPane(Pane pane)
	{
		drawPane = pane;
	}
	
	public MyPolygon getPolygon()
	{
		return p;
	}
	
	private void drawHexP()
	{
		ObservableList<Double> list = p.getPoints();
		list.clear();
		p.setFill(color);
		p.setStroke(color);
		for(int i = 0; i < 6; i++)
		{
			//画一个竖着的正六边形
			list.add(centerX + length * Math.cos(i * Math.PI / 3 + Math.PI / 6));
			list.add(centerY + length * Math.sin(i * Math.PI / 3 + Math.PI / 6));
		}
	}
	
	//立即绘制六边形
	public void drawHex()
	{
		//if(!isAvailable || !isFilled) return;
		if(p == null)
		{
			p = new MyPolygon();
			drawHexP();
			drawPane.getChildren().add(p);
		}
		else
			drawHexP();
		
	}
	
}

//六边形
public class Hex {
	private int score, best;
	private LinkedList<HexBlock> cleaningQueue;	//待消除队列
	private LinkedList<HexBlock> tempQueue;	//临时队列
	private HexBlock[][] hb;		//HexBlock
	private boolean isGameOver;
	private Pane drawPane;
	private Label lb, lb2;
	
	public Hex(int best)
	{
		hb = new HexBlock[9][17];
		cleaningQueue = new LinkedList<HexBlock>();
		tempQueue = new LinkedList<HexBlock>();
		GameInfo.bigHex = this;
		this.best = best;
	}
	
	public HexBlock getBlock(int row, int column)
	{
		return hb[row][column];
	}
	
	//斜线检查
	private int checkLine(int row, int column, int k)
	{
		int r = 0, c = 0, r1, c1;
		//首先找到斜线上的第一个
		for(c1 = column, r1 = row; 
				(r1 >= 0 && c1 >= 0) && (r1 < 9 && c1 < 17) && hb[r1][c1].getAvailable() == true; 
				c1 -= k, r1 = k * (c1 - column) + row)
		{
			r = r1;
			c = c1;
		}
		//下移
		for(c1 = c, r1 = r; 
				(r1 >= 0 && c1 >= 0) && (r1 < 9 && c1 < 17) && hb[r1][c1].getAvailable() == true;
				c1 += k, r1 = k * (c1 - column) + row)
		{
			if(hb[r1][c1].getFillState() == false) 
			{
				tempQueue.clear();
				return 0;
			}
			tempQueue.add(hb[r1][c1]);
		}
		cleaningQueue.addAll(tempQueue);
		tempQueue.clear();
		return 1;
	}
	
	//消除检查
	public int checkClean(int row, int column)
	{
		int n = 0;
		//左斜检查
		n += checkLine(row, column, 1);
		//右斜检查
		n += checkLine(row, column, -1);
		//横向检查
		int c = column, c1;
		//找到横排第一个
		for(c1 = column; c1 >= 0 && hb[row][c1].getAvailable() == true; c1 -= 2)
			c = c1;
		//右移
		for(c1 = c; c1 < 17 && hb[row][c1].getAvailable() == true; c1 += 2)
		{
			if(hb[row][c1].getFillState() == false) 
			{
				tempQueue.clear();
				n--;
				break;
			}
			tempQueue.add(hb[row][c1]);
		}
		cleaningQueue.addAll(tempQueue);
		tempQueue.clear();
		return ++n;
	}
	
	public void blockClean()
	{
		for(int i = 0; i < cleaningQueue.size(); i++)
		{
			HexBlock hbs = cleaningQueue.get(i);
			hbs.setFillState(false);
			FadeTransition ft = new FadeTransition(Duration.millis(500),hbs.getPolygon());
			ft.setFromValue(1);
			ft.setToValue(0);
			ft.setOnFinished(e -> {
				hbs.setColor(Color.rgb(77, 77, 75));
				hbs.drawHex();
				FadeTransition ft2 = new FadeTransition(Duration.millis(500),hbs.getPolygon());
				ft2.setFromValue(0);
				ft2.setToValue(1);
				ft2.play();
			});
			ft.play();
			
		}
		cleaningQueue.clear();
	}
	
	public void addScore(int s)
	{
		score += s;
		lb.textProperty().set("SCORE:" + String.valueOf(score));
		if(score > best) {
			best = score;
			lb2.textProperty().set("BEST:" + String.valueOf(best));
		}
	}
	
	public int getScore()
	{
		return score;
	}
	
	public void initizeHex(double x, double y, double hexSize, Pane pane)
	{
		GameInfo.allVisibleBlock = new HexBlock[9][17];
		isGameOver = false;
		drawPane = pane;
		pane.getChildren().clear();
		//内部构建图形块对象
		int i, j;
		//首先构建全部对象
		for(i = 0; i < 9; i++)
			for(j = 0; j < 17; j++)
			{
				hb[i][j] = new HexBlock(false);
				GameInfo.allVisibleBlock[i][j] = hb[i][j];
			}
		lb = new Label("SCORE:0");	
		lb.setTextFill(Color.WHITE);
		lb.setTranslateX(x + hexSize * 4.2);
		lb.setTranslateY(y + hexSize * 14.2);
		lb.setFont(new Font(40));
		pane.getChildren().add(lb);
		
		lb2 = new Label("BEST:" + best);	
		lb2.setTextFill(Color.WHITE);
		lb2.setTranslateX(x + hexSize * 4.2);
		lb2.setTranslateY(y + hexSize * 15.2);
		lb2.setFont(new Font(40));
		pane.getChildren().add(lb2);
		
		//上面的梯形
		for(i = 0; i < 5; i++)
			for(j = 4 - i; j < 13 + i; j += 2)
				hb[i][j].setAvailable(true);
		//下面的梯形
		for(i = 5; i < 9; i++)
			for(j = i - 4; j < 21 - i; j += 2)
				hb[i][j].setAvailable(true);
		//开始绘制
		//六边形参数
		double xx = hexSize * Math.sqrt(3) / 2, yy = hexSize * 1.5, 
				spaceX = xx / 10, spaceY = yy / 10;
		hb[0][0].setPane(pane);
		for(i = 0; i < 9; i++)
			for(j = 0; j < 17; j++)
			{
				if(hb[i][j].getAvailable() == true)
				{
					hb[i][j].setHex(x + j * (xx + spaceX), y + i * (yy + spaceY), hexSize);
					hb[i][j].drawHex();
					hb[i][j].getPolygon().setRow(i);
					hb[i][j].getPolygon().setColumn(j);
				}
			}
				
		//to be continue
	}
	
	public void setGameOver()
	{
		isGameOver = true;
		Button bt = new Button("重新开始");
		bt.setOnMouseClicked(e ->{
			//Hex hex = new Hex();
			drawPane.setStyle("-fx-background-color: #20201e");
			HexBlockCreate hbc = new HexBlockCreate(3);
			this.initizeHex(100, 85, 40, drawPane);
			hbc.createBlock(0);
			hbc.createBlock(1);
			hbc.createBlock(2);
			hbc.drawBlock(0, 900, 100, 30, drawPane);
			hbc.drawBlock(1, 900, 300, 30, drawPane);
			hbc.drawBlock(2, 900, 500, 30, drawPane);
			this.addScore(-getScore());
			drawPane.getChildren().remove(bt);
		});
		drawPane.getChildren().add(bt);
		
	}
}

class HexBlockCreate
{
	private Random r;
	private HexBlock[][][] next;
	private int total;//会出现几个next
	private LinkedList<MyPolygon>[] list;
	private Pane drawPane;
	
	public int getTotal()
	{
		return total;
	}
	
	public HexBlockCreate(int n)
	{
		list = new LinkedList[n];
		GameInfo.dragBlock = new LinkedList<HexBlock>(); 
		GameInfo.tempList = new LinkedList<HexBlock>(); 

		r = new Random();
		total = n;
		next = new HexBlock[total][4][7];
		int i, j, k;
		for(i = 0; i < total; i++)
			for(j = 0; j < 4; j++)
				for(k = 0; k < 7; k++)
					next[i][j][k] = new HexBlock(false);
		
		//构造next六边形块
		for(i = 0; i < total; i++)
		{
			list[i] = new LinkedList<MyPolygon>();
			for(k = 0; k < 3; k += 2)
			{
				next[i][k][1].setAvailable(true);
				next[i][k][3].setAvailable(true);
			}
			for(j = 0; j < 7; j += 2)
				next[i][1][j].setAvailable(true);
			next[i][3][0].setAvailable(true);
			next[i][3][4].setAvailable(true);
		}
	}
	
	public boolean createBlock(int n)
	{
		if(n >= total) return false;
		
		for(int j = 0; j < 4; j++)
			for(int k = 0; k < 7; k++)
				next[n][j][k].setFillState(false);
		
		LinkedList<HexBlock> list = new LinkedList<HexBlock>();
		
		int random = r.nextInt(25);//共有25钟砖块
		if(random < 5)
		{
			list.add(next[n][1][0]);
			list.add(next[n][1][2]);
			list.add(next[n][1][4]);
			switch(random)
			{
			case 0: list.add(next[n][0][1]); break;
			case 1: list.add(next[n][0][3]); break;
			case 2: list.add(next[n][2][1]); break;
			case 3: list.add(next[n][2][3]); break;
			case 4: list.add(next[n][1][6]); break;
			}
		}
		else if(random < 10)
		{
			list.add(next[n][0][1]);
			list.add(next[n][1][2]);
			list.add(next[n][2][3]);
			switch(random)
			{
			case 5: list.add(next[n][1][0]); break;
			case 6: list.add(next[n][0][3]); break;
			case 7: list.add(next[n][2][1]); break;
			case 8: list.add(next[n][1][4]); break;
			case 9: list.add(next[n][3][4]); break;
			}
		}
		else if(random < 15)
		{
			list.add(next[n][0][3]);
			list.add(next[n][1][2]);
			list.add(next[n][2][1]);
			switch(random)
			{
			case 10: list.add(next[n][1][0]); break;
			case 11: list.add(next[n][0][1]); break;
			case 12: list.add(next[n][2][3]); break;
			case 13: list.add(next[n][1][4]); break;
			case 14: list.add(next[n][3][0]); break;
			}
		}
		else if(random == 15)
			list.add(next[n][1][2]);//一个点
		else if(random < 19)
		{
			list.add(next[n][0][3]);
			list.add(next[n][1][2]);
			switch(random)
			{
			case 16: list.add(next[n][1][4]); list.add(next[n][2][3]); break;
			case 17: list.add(next[n][1][0]); list.add(next[n][0][1]); break;
			case 18: list.add(next[n][1][4]); list.add(next[n][0][1]); break;
			}
			
		}
		else//半包围的圈(高难度)
		{
			switch(random)
			{
			case 19: list.add(next[n][1][0]); list.add(next[n][1][4]); list.add(next[n][0][1]); list.add(next[n][0][3]); break;
			case 20: list.add(next[n][1][0]); list.add(next[n][1][4]); list.add(next[n][2][1]); list.add(next[n][2][3]); break;
			case 21: list.add(next[n][1][0]); list.add(next[n][0][1]); list.add(next[n][2][1]); list.add(next[n][0][3]); break;
			case 22: list.add(next[n][1][0]); list.add(next[n][0][1]); list.add(next[n][2][1]); list.add(next[n][2][3]); break;
			case 23: list.add(next[n][1][4]); list.add(next[n][0][3]); list.add(next[n][2][3]); list.add(next[n][0][1]); break;
			case 24: list.add(next[n][1][4]); list.add(next[n][0][3]); list.add(next[n][2][3]); list.add(next[n][2][1]);break;
			}
		}
		
		Color c = Color.WHITE;
		switch(r.nextInt(5))
		{
			case 0: c = Color.rgb(254, 137, 181);break;
			case 1: c = Color.rgb(245, 162, 110);break;
			case 2: c = Color.rgb(113, 223, 150);break;
			case 3: c = Color.rgb(255, 220, 138);break;
			case 4: c = Color.rgb(137, 140, 255);break;
		}
		for(int i = 0; i < list.size(); i++)
		{
			list.get(i).setColor(c);
			list.get(i).setFillState(true);
		}
		return true;
	}
	
	class RC
	{
		int r;
		int c;
		RC(int r, int c)
		{
			this.r = r;
			this.c = c;
		}
	}
	
	private void startExchange(LinkedList<HexBlock> list1, LinkedList<RC> list2)
	{
		int i;
		//清理图形对象，并生成新的对象
		Color col = list1.get(0).getColor();
		int n = list1.get(0).getPolygon().getN();
		for(i = 0; i < list1.size(); i++)
		{
			MyPolygon mp = list1.get(i).getPolygon();
			drawPane.getChildren().removeAll(mp);
			int r = mp.getRow(), c = mp.getColumn();
			setHexBlock(n, r, c);
		}
			
		
		for(i = 0; i < list2.size(); i++)
		{
			RC rc = list2.get(i);
			int r = rc.r, c = rc.c;
			
			next[n][r][c].setColor(col);
			next[n][r][c].setFillState(true);
		}
	}
	
	private RC roateBlock(int r, int c)//令一个方块围绕中心旋转60度
	{
		//由于采用的六边形坐标并不规范,且坐标变换较简单。故采用枚举
		int rr = r, cc = c;
		switch(r)
		{
		case 0: if(c == 1) cc = 3; 
			else if(c == 3) {cc = 4; rr = 1;} break;
		
		case 1: if(c == 0) {cc = 1; rr = 0;}
			else if(c == 4){cc = 3; rr = 2;} 
			else if(c == 6){cc = 4; rr = 3;} break;
		
		case 2: if(c == 1) {cc = 0; rr = 1;}
			else if(c == 3) cc = 1; break;
			
		case 3: if(c == 4) cc = 0;
			else if(c == 0){cc = 6; rr = 1;} break;
		}
		return new RC(rr, cc);
	}
	
	
	public void rotateHex(int n)
	{
		LinkedList<HexBlock> list1 = new LinkedList<HexBlock>();
		LinkedList<RC> list2 = new LinkedList<RC>();
		
		int i, j;
		for(i = 0; i < 4; i++)
			for(j = 0; j < 7; j++)
				if(next[n][i][j].getFillState())
				{
					list1.add(next[n][i][j]);
					list2.add(roateBlock(i, j));
				}
		
		startExchange(list1, list2);
	}
	
	public boolean drawBlock(int n, double x, double y, double hexSize, double specialSppace)
	{
		if(n >= total) return false;
		double xx = hexSize * Math.sqrt(3) / 2, yy = hexSize * 1.5, 
				spaceX = xx / 10, spaceY = yy / 10;
		
		
		
		for(int j = 0; j < 4; j++)
			for(int k = 0; k < 7; k++)
				if(next[n][j][k].getAvailable() && next[n][j][k].getFillState())
				{
					next[n][j][k].setHex(x + k * (xx + spaceX + specialSppace), y + j * (yy + spaceY + specialSppace), hexSize);
					next[n][j][k].drawHex();
					next[n][j][k].setShadow();
					
					next[n][j][k].getPolygon().setRow(j);
					next[n][j][k].getPolygon().setColumn(k);
					next[n][j][k].getPolygon().setN(n);

					
					next[n][j][k].getPolygon().setOnMousePressed(new BlockDragStart(this));
					
					next[n][j][k].getPolygon().setOnMouseDragged(new BlockDrag(this));
					
					next[n][j][k].getPolygon().setOnMouseReleased(new BlockDragDone(this));
					
				}
		return true;
	}
	
	public boolean drawBlock(int n, double x, double y, double hexSize, Pane pane)
	{
		next[n][0][0].setPane(pane);
		this.drawPane = pane;
		return drawBlock(n, x, y, hexSize, 0);
	}
	
	public HexBlock getHexBlock(int n, int i, int j)
	{
		return next[n][i][j];
	}
	
	public void setHexBlock(int n, int i, int j)
	{
		HexBlock hb = new HexBlock(true);
		next[n][i][j] = hb;
	}
	
}

class BlockDragStart implements EventHandler<MouseEvent>
{
	private static double startX, startY;
	private static int n;
	private HexBlockCreate hbc;
	static int row, column;
	
	
	public void handle(MouseEvent e)
	{
		BlockDragStart.n = ((MyPolygon)e.getSource()).getN();
		if(e.getButton() == MouseButton.PRIMARY)
		{
			BlockDragStart.row = ((MyPolygon)e.getSource()).getRow();
			BlockDragStart.column = ((MyPolygon)e.getSource()).getColumn();
			
			startX = e.getX();
			startY = e.getY();
			GameInfo.dragColor = (Color) ((MyPolygon)e.getSource()).getFill();
			GameInfo.dragBlock.clear();
			for(int i = 0; i < 4; i++)
				for(int j = 0; j < 7; j++)
					if(hbc.getHexBlock(BlockDragStart.n, i, j).getAvailable() && hbc.getHexBlock(BlockDragStart.n, i, j).getFillState())
						GameInfo.dragBlock.add(hbc.getHexBlock(BlockDragStart.n, i, j));
		}
		else if(e.getButton() == MouseButton.SECONDARY)
		{
			hbc.rotateHex(BlockDragStart.n);
			hbc.drawBlock(BlockDragStart.n, 900, (BlockDragStart.getN() + 1) * 200 - 100, 30, 0);
		}
	}
	
	BlockDragStart(HexBlockCreate hbc)
	{
		this.hbc = hbc;
	}
	
	public static double getStartX()
	{
		return startX;
	}
	
	public static double getStartY()
	{
		return startY;
	}
	
	public static int getN()
	{
		return n;
	}
	
	public static void setN(int n)
	{
		BlockDragStart.n = n;
	}
}

class BlockDrag implements EventHandler<MouseEvent>
{
	protected HexBlockCreate hbc;
	
	public BlockDrag(HexBlockCreate hbc)
	{
		this.hbc = hbc;
	}
	
	public void handle(MouseEvent e)
	{
		if(e.getButton() == MouseButton.PRIMARY)
		{
			boolean isExe = false;
			hbc.drawBlock(BlockDragStart.getN(), 900 - BlockDragStart.getStartX()+ e.getX(), (BlockDragStart.getN() + 1) * 200 - 100 - BlockDragStart.getStartY()+ e.getY(), 30, 7);
			for(int i = 0; i < 9; i++)
				for(int j = 0; j < 17; j++)
					if(GameInfo.allVisibleBlock[i][j].getAvailable())
						if(GameInfo.allVisibleBlock[i][j].getPolygon().contains(e.getX(), e.getY()))
						{
							int r, c;
							if(!GameInfo.tempList.isEmpty())
							{
								for(int k = 0; k < GameInfo.tempList.size(); k++)
								{
									GameInfo.tempList.get(k).setColor(Color.rgb(77, 77, 75));
									GameInfo.tempList.get(k).drawHex();
								}
								GameInfo.tempList.clear();
							}
							for(int k = 0; k < GameInfo.dragBlock.size(); k++)
							{
								r = GameInfo.dragBlock.get(k).getPolygon().getRow() - BlockDragStart.row;
								c = GameInfo.dragBlock.get(k).getPolygon().getColumn() - BlockDragStart.column;
								if(GameInfo.available(i + r, j + c))
									GameInfo.tempList.add(GameInfo.allVisibleBlock[i + r][j + c]);
								else
								{
									GameInfo.tempList.clear();
									break;
								}
							}
							
							for(int k = 0; k < GameInfo.tempList.size(); k++)
							{
								GameInfo.tempList.get(k).setColor(GameInfo.dragColor.darker().darker());
								GameInfo.tempList.get(k).drawHex();
							}
							
							isExe = true;
							break;
						}
			if(!isExe)
				if(!GameInfo.tempList.isEmpty())
				{
					for(int k = 0; k < GameInfo.tempList.size(); k++)
					{
						GameInfo.tempList.get(k).setColor(Color.rgb(77, 77, 75));
						GameInfo.tempList.get(k).drawHex();
					}
					GameInfo.tempList.clear();
				}
		}	
	}
	
}

class BlockDragDone extends BlockDrag
{

	public BlockDragDone(HexBlockCreate hbc) {
		super(hbc);
	}
	
	/*private void getAvailable()
	{
		
	}*/
	
	public void handle(MouseEvent e)
	{
		if(e.getButton() == MouseButton.PRIMARY)
		{
			hbc.drawBlock(BlockDragStart.getN(), 900, (BlockDragStart.getN() + 1) * 200 - 100, 30, 0);
			if(!GameInfo.tempList.isEmpty())
			{
				for(int k = 0; k < GameInfo.tempList.size(); k++)
				{
					HexBlock block = GameInfo.tempList.get(k);
					block.setColor(GameInfo.dragColor);
					block.setFillState(true);
					block.drawHex();
				}
				int sss = 0;
				for(int k = 0; k < GameInfo.tempList.size(); k++)
				{
					//检查各个点的消除情况
					MyPolygon hb = GameInfo.tempList.get(k).getPolygon();
					sss += GameInfo.bigHex.checkClean(hb.getRow(), hb.getColumn());
				}
				GameInfo.bigHex.blockClean();
				GameInfo.bigHex.addScore(sss * sss * 100 + 40);
				
				//清理图形对象**
				for(int k = 0; k < GameInfo.dragBlock.size(); k++)
				{
					MyPolygon p = GameInfo.dragBlock.get(k).getPolygon();
					((Pane)p.getParent()).getChildren().remove(p);
					hbc.setHexBlock(BlockDragStart.getN(), p.getRow(), p.getColumn());
				}
				
				hbc.createBlock(BlockDragStart.getN());
				hbc.drawBlock(BlockDragStart.getN(), 900, (BlockDragStart.getN() + 1) * 200 - 100, 30, 0);
			
				GameInfo.tempList.clear();
				
				//判断游戏结束
				LinkedList<HexBlock> tempList = new LinkedList<HexBlock>();
				boolean canPut = false;
				for(int k = 0; k < hbc.getTotal() && !canPut; k++)
				{
					for(int i = 0; i < 4; i++)
						for(int j = 0; j < 7; j++)
							if(hbc.getHexBlock(k, i, j).getAvailable() && hbc.getHexBlock(k, i, j).getFillState())
								tempList.add(hbc.getHexBlock(k, i, j));
					
					int r0 = tempList.get(0).getPolygon().getRow(),
							c0 = tempList.get(0).getPolygon().getColumn();
					int[] rm0 = new int[tempList.size()], 
							cm0 = new int[tempList.size()];
					//System.out.println(r0 + "," + c0);
					
					for(int m = 1; m < tempList.size(); m++)
					{			
						rm0[m] = tempList.get(m).getPolygon().getRow() - r0;
						cm0[m] = tempList.get(m).getPolygon().getColumn() - c0;
						//System.out.println(tempList.get(m).getPolygon().getRow() + "," + tempList.get(m).getPolygon().getColumn());
					}
					//System.out.println("");
					for(int i = 0; i < 9 && !canPut; i++)
						for(int j = 0; j < 17 && !canPut; j++)
							if(GameInfo.available(i, j))
							{
								if(tempList.size() == 1){canPut = true;}
								for(int m = 1; m < tempList.size(); m++)
									if(GameInfo.available(i + rm0[m], j + cm0[m]))
										canPut = true;
									else
									{
										canPut = false;
										break;
									}	
							}
					tempList.clear();
				}
				
				if(!canPut)
				{
					//System.out.println(canPut);
					GameInfo.bigHex.setGameOver();
				}	
			}
		}
		BlockDragStart.setN(-1);
	}
}

