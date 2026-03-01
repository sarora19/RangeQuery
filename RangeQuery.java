import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import java.util.Calendar;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




public final class RangeQuery {

	private static Scanner reader;
    
    public static void main(String[] args) throws ParseException, IOException {
    	    //C:\Users\saksh\Desktop\desktop\ProbabilisticWorkspace\RangeQuery\Data\TrajectoryData
    	String fileName = "Data/TrajectoryData/DataSet.xlsx";
        
    	try { 
    	reader = new Scanner(System.in);  
    	System.out.println("Enter rectangular region coordinates(Minx, Maxy, Maxx, Miny): ");
    	double x1 = reader.nextDouble();
    	double y1 = reader.nextDouble();
    	double x2 = reader.nextDouble();
    	double y2 = reader.nextDouble();
    	System.out.println("Enter probabilistic threshold: ");
    	float threshold = reader.nextFloat();
    	
    	DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    	//For valid output use below values
//    	-8.62 41.14 -8.55 41.15 (Minx, Maxy, Maxx, Miny coordinates)
//    	2013-06-30T20:38:55 (start time)
//    	2013-06-30T22:38:55 (end time)
    	//Start date/time
    	System.out.println("Enter start time in yyyy-MM-ddTHH:mm:ss format:");
		Date startTime = null;
		while (startTime == null) {
		String line = reader.next();
		try {
			if( line != null && !line.isEmpty()) {
				startTime = dateTimeFormat.parse(line);
			}
		} catch (ParseException e) {
			System.out.println("Sorry, start time is not valid. Please provide valid input.");
			}
		}

		//End date/time
		System.out.println("Enter end time in yyyy-MM-ddTHH:mm:ss format:");
		Date endTime = null;
		while (endTime == null) {
		String line = reader.nextLine();
		try {
			if( line != null && !line.isEmpty()) {
			endTime = dateTimeFormat.parse(line);
			}
		} catch (ParseException e) {
			System.out.println("Sorry, end time is not valid. Please provide valid input.");
			}
		}
    	
	 	System.err.close();
    	System.setErr(System.out);
    	
		//Read trajectory data from excel file 
    	FileInputStream file = new FileInputStream(new File(fileName));
        //Create Workbook instance holding reference to .xlsx file
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        //Get first/desired sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        Boolean flag;
        Map<Integer, Double> taxiInQueryRegionList = new HashMap<Integer, Double>();
        
        while (rowIterator.hasNext())
        {
        	flag = false;
        	
            ArrayList<String> xyValues = new ArrayList<String>(30);
            ArrayList<Double> xValues = new ArrayList<Double>(30);
            ArrayList<Double> yValues= new ArrayList<Double>(30);
            ArrayList<String> timeStampList = new ArrayList<String>();
            int taxiId = 0;
            double maxXValue = 0;
            double maxYValue = 0;
            double minXValue = 0;
            double minYValue = 0;	
            int i=0;
            int startTimeStamp=0;
            
            Row row = rowIterator.next();
            //For each row, iterate through all the columns
            Iterator<Cell> cellIterator = row.cellIterator();
            
            
            
            
            while (cellIterator.hasNext())
            {
            	Cell cell = cellIterator.next();
                int index = cell.getColumnIndex();      

                //Fetch data from dataset
                if(index == 8) {
                   //Polyline column
                   String points = cell.getStringCellValue();
                   String[] pt = points.split("], ");
                  
                   //Push xy values and x/y values
                   for ( i = 0; i < pt.length - 1; i++) {
                	   String xyFinalPoints = pt[i].replace("[","");
                       xyValues.add(xyFinalPoints);
                       String[] xy = xyFinalPoints.split(", ");
                       xValues.add(Double.parseDouble(xy[0]));
                       yValues.add(Double.parseDouble(xy[1]));
                   }
                 
                   //Get min/max x values
                   if(!xValues.isEmpty()) {
                	  maxXValue =  Collections.max(xValues);
                      minXValue = Collections.min(xValues);
                   }

                   //Get min/max y values
                   if(!yValues.isEmpty()) {
                	   maxYValue = Collections.max(yValues);
                       minYValue = Collections.min(yValues);
                    }
               } 	
               else if(index == 4) {
            	  //Taxi id column
            	  if( cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            		  //Get taxi id
            		  taxiId=(int)cell.getNumericCellValue();	
            	  }  
                }else if(index == 5) {
                	//Time stamp column
                	if( cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                		startTimeStamp= (int) cell.getNumericCellValue();  
                	}
                }  
                 index++;
                }
	            
            	//Code to frame timeStamplist from dataset
	            Calendar cal = Calendar.getInstance();
	            String startOfTime1 = (String) new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(startTimeStamp * 1000L));
	            Date startDate = dateTimeFormat.parse(startOfTime1);
	               
	            for(i=0; i<xyValues.size(); i++)
	            {    	
	                cal.setTime(startDate);
	                cal.add(Calendar.SECOND, 15);
	                startDate=cal.getTime();
	                String startDate1= dateTimeFormat.format(startDate);
	                timeStampList.add(startDate1);  
	            }
	            
	          //Code to compare timestamp with the entered start and end time
	          for(i=0; i<timeStampList.size(); i++)
              {  
           		  if(dateTimeFormat.parse(timeStampList.get(i)).compareTo(startTime) >= 0 && dateTimeFormat.parse(timeStampList.get(i)).
           				  compareTo(endTime) >= 0)
                    {
           			  	flag = true;
                    }
               }
	         
	          
	          if(flag) {
	          Rectangle2D rect_KLMN = new Rectangle2D.Double(x1, y1, Math.max(x2,x1) - Math.min(x2,x1), Math.max(y2,y1) - Math.min(y2,y1));
	          
	          Point2D[] objectPoints = new Point2D[2000];
	          double count =0;
	          for(i=0; i<xValues.size(); i++)
        	  {
	        	  Double x = xValues.get(i);
//	        	  System.out.println(x+"xvaule");
	        	  Double y = yValues.get(i);
//	        	  System.out.println(y+"yvaule");
	        	  objectPoints[i] = new Point2D.Double(x,y);
//	        	  System.out.println("objectPoints[i]"+ objectPoints[i]);
//	        	  System.out.println(rect_KLMN+"rect_KLMN");
//	        	  System.out.println("what Value?"+rect_KLMN.contains(objectPoints[i]));
	        	  if (rect_KLMN.contains(objectPoints[i]) == true)
	        	  {		
	        		  count++;
	        	  }
        	  }
	          
	          
	          //overlapping rectangle and probability comparison against threshold entered
	        	  double TotalPoints = xyValues.size();
//	        	  System.out.println("count"+count);
//	        	  System.out.println("TotalPoints"+TotalPoints);
	        	  Double ind_Prob = (count/TotalPoints);
	        	 
	        	  if(ind_Prob >= threshold) {
	           		taxiInQueryRegionList.put(taxiId,ind_Prob);
		           	  }      
	          }
	          
	        }
        
        	if(taxiInQueryRegionList.size() > 0) {
        		
        		System.out.println("\nTaxis that lie within the query region are:" + taxiInQueryRegionList.size() + "\n");
        	
        		System.out.println("TaxiId\tProbability");
        		for(Map.Entry m:taxiInQueryRegionList.entrySet()){  
        			System.out.println(m.getKey() + "   " + m.getValue());  
        		}  
        	}
        	else {
        		System.out.println("We are really sorry no taxis are available within specified range.");
        	}
        	
        	file.close();
    	}
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file ");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
             ex.printStackTrace();
        }
    	catch(ParseException ex) {
    		 System.out.println(ex);
    	}
    }

//	private static boolean contains(Rectangle2D rect_KLMN, Point objectPoints) {
//		// TODO Auto-generated method stub
//		  return objectPoints.x >= rect_KLMN.getX() && objectPoints.y >= rect_KLMN.getY() && objectPoints.x <= rect_KLMN.getX() + rect_KLMN.getWidth() && objectPoints.y <= rect_KLMN.getY() + rect_KLMN.getHeight(); 
//	}

	public static double IntersectedArea(double K, double L, double M, double N, double P, double Q, double R, double S) {
		double area1_ ,area2_ ,areaInt_ = 0;
        Rectangle2D rect_KLMN = new Rectangle2D.Double(K, L, Math.max(M,K) - Math.min(M,K), Math.max(N,L) - Math.min(N,L));
        Rectangle2D rect_PQRS = new Rectangle2D.Double(P, Q, Math.max(R,P) - Math.min(R,P), Math.max(S,Q) - Math.min(S,Q));
        
        area1_ = (double) (rect_KLMN.getHeight() * rect_KLMN.getWidth());
        area2_ = (double) (rect_PQRS.getHeight() * rect_PQRS.getWidth());

        if (rect_KLMN.intersects(rect_PQRS)) {
            Rectangle2D rect_int = rect_KLMN.createIntersection(rect_PQRS);
            areaInt_ = (rect_int.getHeight() * rect_int.getWidth());
        }else {
        	areaInt_ = 0;
        }
        return areaInt_;
    }

}
class Point {
    public Point(Double x2, Double y2) {
		// TODO Auto-generated constructor stub
	}
	double x;
    double y;
}
