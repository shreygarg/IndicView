#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <bits/stdc++.h>

using namespace std;
using namespace cv;

extern "C" {
int noow=0;
int retur=0;
cv::Mat erode(cv::Mat im,int kernel){
  cv::Mat res;
  cv::Mat element = cv::getStructuringElement(2,cv::Size(2*kernel+1,2*kernel+1),cv::Point(kernel,kernel));
  cv::erode(im,res,element);
  return res;
}

cv::Mat dilate(cv::Mat im,int kernel){
  cv::Mat res;
  cv::Mat element = cv::getStructuringElement(2,cv::Size(2*kernel+1,2*kernel+1),cv::Point(kernel,kernel));
  cv::dilate(im,res,element);
  return res;
}
std::vector<std::pair<int,int> > arrange_words(std::vector<std::pair<int,int> > all_words,int maxBlobHeight){
  int size=all_words.size();
  std::cout<<"maxBlobHt:"<<maxBlobHeight<<std::endl;
  std::vector<std::pair<int,int> > new_words(size);
  std::vector<int>* neighbour = new std::vector<int>[size];
  int* row_determined = new int[size];
	int* parent = new int[size];
  for(int i=0;i<size;i++){
    row_determined[i]=0;
		parent[i] = 0;
  }
  for(int i=0;i<size;i++){
    for(int j=0;j<size;j++){
			if(i==j)continue;
			if(abs(all_words[i].first - all_words[j].first)< 0.6*maxBlobHeight){
				neighbour[i].push_back(j);
				//neighbour[j].push_back(i);
			}
		}
  }
  int rows_assigned = 0 ;
	int row_count = 1;
  while(rows_assigned==0){
    int topmost;
		for(int i=0;i<size;i++){
			if(row_determined[i]==0){
				topmost = i;
				break;
			}
		}

    for(int i=0;i<size;i++){
			//std::cout<<row_determined[i]<<" ";
			if(row_determined[i]==0 && all_words[i].first<all_words[topmost].first)topmost=i;
		}
		//std::cout<<topmost<<std::endl;
		//cv::waitKey(0);
		int row_num=-1;
		for(int j=0;j<neighbour[topmost].size();j++){
			if(row_determined[neighbour[topmost][j]]!=0){
				if(row_num == -1){
					row_num=row_determined[neighbour[topmost][j]];
					parent[topmost]=neighbour[topmost][j];
				}
				else if(row_num!=-1 && row_determined[neighbour[topmost][j]]!=row_num){
					if((abs(all_words[topmost].first-all_words[neighbour[topmost][j]].first)+abs(all_words[topmost].second-all_words[neighbour[topmost][j]].second))< (abs(all_words[topmost].first-all_words[parent[topmost]].first)+abs(all_words[topmost].second-all_words[parent[topmost]].second))){
						row_num = row_determined[neighbour[topmost][j]];
						parent[topmost]=neighbour[topmost][j];
					}
				}
			}
		}
		if(row_num==-1){
			row_determined[topmost] = row_count;
			row_count++;
		}
		else row_determined[topmost] = row_num;

		rows_assigned = 1;
    for(int i=0;i<size;i++){
      if(row_determined[i]==0){
        rows_assigned=0;
        break;
      }
    }
  }

	std::cout<<"neighbour array:"<<std::endl;
	for(int i=0;i<size;i++){
		std::cout<<i<<":";
		for(int j=0;j<neighbour[i].size();j++){
			std::cout<<neighbour[i][j]<<" ";
		}
		std::cout<<std::endl;
	}

	std::cout<<"to be arranged words:"<<std::endl;
	for(int i=0;i<size;i++){
		std::cout<<i<<" "<<all_words[i].second<<" "<<all_words[i].first<<" "<<row_determined[i]<<std::endl;
	}
	std::cout<<std::endl;

	int entry = 0;
	for(int i=1;i<=row_count;i++){
		int row_entry = 0;
		for(int j=0;j<size;j++){
			if(row_determined[j]==i){
				row_entry++;
			}
			else continue;
			int greater_than = 0;
			for(int k=0;k<size;k++){
				if(row_determined[k]==i && k!=j && all_words[k].second<all_words[j].second){
					greater_than++;
				}
			}
			new_words[entry+greater_than] = all_words[j];
		}
		entry += row_entry;
	}
  return new_words;
}
cv::Mat restructure(cv::Mat im){
  cv::Mat res(im.rows+8,im.cols+8,CV_8UC1);
  for(int i=0;i<res.rows;i++){
    for(int j=0;j<res.cols;j++){
      res.at<uchar>(i,j)=255;
    }
  }
  for(int i=0;i<im.rows;i++){
    for(int j=0;j<im.cols;j++){
      res.at<uchar>(i+4,j+4)=im.at<uchar>(i,j);
    }
  }
  return res;
}

cv::Mat clearImage(cv::Mat original, int* valid,int** A){
  for(int i=0;i<original.rows;i++){
    for(int j=0;j<original.cols;j++){
      if(valid[A[i][j]]==0)original.at<uchar>(i,j)=255;
    }
  }
  return original;
}

cv::Mat blob_joined(cv::Mat bin,cv::Mat original,int name,int* final_count,std::string file_name,int wordid){
  cv::Point* lt,*rb,*center;
  int** A= new int*[bin.rows];
  for(int i=0;i<bin.rows;i++)A[i]=new int[bin.cols];
  for(int i=0;i<bin.rows;i++)
    for(int j=0;j<bin.cols;j++)
      A[i][j]=-1;
  int count=0;
  std::queue<cv::Point> q;
  for(int i=0;i<bin.rows;i++){
    for(int j=0;j<bin.cols;j++){
      if((int)bin.at<uchar>(i,j)==0 && A[i][j]==-1){
        q.push(cv::Point(i,j));
        while(q.size()!=0){
          cv::Point p=q.front();
          if(A[p.x][p.y]==-1){
            count++;
          }
          for(int k=p.x-1;k<=p.x+1;k++){
            for(int l=p.y-1;l<=p.y+1;l++){
              if((k>=0) && (l>=0) && (k<bin.rows) && (l<bin.cols) && ((int)bin.at<uchar>(k,l)==0) && (A[k][l]==-1)){
                q.push(cv::Point(k,l));
                A[k][l]=0;
              }
            }
          }
          A[p.x][p.y]=count;
          q.pop();
        }
      }
    }
  }
  //std::cout<<"count="<<count<<std::endl;
  lt=new cv::Point[count+1];
  rb=new cv::Point[count+1];
  center=new cv::Point[count+1];
  int* valid=new int[count+1];
  int* hist = new int[count+1];
  for(int a=0;a<=count;a++){
    lt[a]=cv::Point(bin.cols,bin.rows);
    rb[a]=cv::Point(0,0);
    hist[a]=0;
    valid[a]=1;
  }
  for(int i=0;i<bin.rows;i++){
    for(int j=0;j<bin.cols;j++){
      if(A[i][j]!=-1){
        hist[A[i][j]]++;
        if(i<lt[A[i][j]].y)lt[A[i][j]].y=i;
        if(j<lt[A[i][j]].x)lt[A[i][j]].x=j;
        if(i>rb[A[i][j]].y)rb[A[i][j]].y=i;
        if(j>rb[A[i][j]].x)rb[A[i][j]].x=j;
      }
    }
  }
  int maxBlobHeight=0;
  for(int a=1;a<=count;a++){
    center[a].x=(lt[a].x+rb[a].x)/2;
    center[a].y=(lt[a].y+rb[a].y)/2;
    //std::cout<<center[a].x<<" "<<center[a].y<<std::endl;
    if(hist[a] < (bin.rows*bin.cols)/5000) valid[a]=0;
    if(lt[a].x==0 || rb[a].x==bin.cols-1 || lt[a].y==0 || rb[a].y==bin.rows-1)valid[a]=0;

    if(valid[a] && ((rb[a].y-lt[a].y)>maxBlobHeight))maxBlobHeight=(rb[a].y-lt[a].y);
  }

  original = clearImage(original,valid,A);
  //cv::imshow("clear",original);

  for(int a=1;a<=count;a++){
    for(int b=a+1;b<=count;b++){
      if(valid[a]==0 || valid[b]==0) continue;
      if((center[b].y-center[a].y)>maxBlobHeight)continue;
      if((center[b].y-center[a].y)<(rb[a].y-lt[a].y) && (center[b].x)<=(rb[a].x) && (center[b].x)>=(lt[a].x)){
        //std::cout<<"1:"<<b<<std::endl;
        //std::cout<<lt[b].x<<","<<lt[b].y<<" "<<rb[b].x<<","<<rb[b].y<<std::endl;
        //std::cout<<lt[a].x<<","<<lt[a].y<<" "<<rb[a].x<<","<<rb[a].y<<std::endl;
        valid[b]=0;
        if(rb[b].x>rb[a].x)rb[a].x=rb[b].x;
        if(rb[b].y>rb[a].y)rb[a].y=rb[b].y;
        if(lt[b].x<lt[a].x)lt[a].x=lt[b].x;
        if(lt[b].y<lt[a].y)lt[a].y=lt[b].y;
        rb[b]=rb[a];
        lt[b]=lt[a];
        center[a].x=(lt[a].x+rb[a].x)/2;
        center[a].y=(lt[a].y+rb[a].y)/2;
      }
      else if((center[b].y-center[a].y)<(rb[b].y-lt[b].y) && (center[a].x)<=(rb[b].x) && (center[a].x)>=(lt[b].x)){
        //std::cout<<"2:"<<a<<std::endl;
        //std::cout<<lt[b].x<<","<<lt[b].y<<" "<<rb[b].x<<","<<rb[b].y<<std::endl;
        //std::cout<<lt[a].x<<","<<lt[a].y<<" "<<rb[a].x<<","<<rb[a].y<<std::endl;
        valid[a]=0;
        if(rb[b].x<rb[a].x)rb[b].x=rb[a].x;
        if(rb[b].y<rb[a].y)rb[b].y=rb[a].y;
        if(lt[b].x>lt[a].x)lt[b].x=lt[a].x;
        if(lt[b].y>lt[a].y)lt[b].y=lt[a].y;
        lt[a]=lt[b];
        rb[a]=rb[b];
        center[b].x=(lt[b].x+rb[b].x)/2;
        center[b].y=(lt[b].y+rb[b].y)/2;
      }
    }
  }
  //std::cout<<std::endl;
  cv::Mat bound=bin.clone();
  cv::Mat temp;
  int fc=0;
  std::vector<std::pair<int,int> > all_words;
  for(int a=1;a<=count;a++){
    if(valid[a]){
      if((rb[a].y-lt[a].y)*(rb[a].x-lt[a].x) < 100) continue;
      std::pair<int,int> p;
      p.first=center[a].y;
      p.second=center[a].x;
      all_words.push_back(p);
    }
  }
  std::sort(all_words.begin(), all_words.end());
  all_words=arrange_words(all_words,maxBlobHeight);
  if(wordid>=all_words.size())
  {
	  retur=-1;
	  return original;
  }
  int i=wordid;
    int a;
    for(int j=1;j<=count;j++){
      if((center[j].y==all_words[i].first) && (center[j].x==all_words[i].second)) a=j;
    }
      std::stringstream ss;
      ss << a;
      std::string t=ss.str();
      cv::rectangle(bound,lt[a],rb[a],cv::Scalar(250),1);
      if(lt[a].x!=rb[a].x && lt[a].y!=rb[a].y){
        fc++;
        //std::cout<<lt[a].x<<","<<lt[a].y<<" "<<rb[a].x<<","<<rb[a].y<<" center->"<<center[a].x<<" "<<center[a].y<<std::endl;
        temp = original(cv::Rect(lt[a].x,lt[a].y,rb[a].x - lt[a].x,rb[a].y - lt[a].y));
        std::ostringstream oss2;
        //oss2<<"/home/arna/Desktop/IITKGP/projects/Pilot/SauvolaMS_Images_words/"<<name<<"_"<<fc<<".jpg";
        //cv::imwrite(oss2.str() , temp);
        /*cv::namedWindow("output",1);
        cv::imshow("output",temp);*/
        temp=restructure(temp);
        //useTess(temp,file_name);   // Pass it to Tesseract API
        if(all_words.size()==i-1)
          retur=-1;
        return temp;
        //cv::waitKey(0);
      }
  //std::cout<<"image size:"<<original.rows<<" "<<original.cols<<";"<<bin.rows<<" "<<bin.cols<<std::endl;
  *final_count=fc;
  cv::Mat tmp(20,20,CV_8UC1);
  tmp.setTo(Scalar(255));
  return tmp;
}

cv::Mat slow_sauvola_binarize(cv::Mat img,int w,float k){
  cv::Mat sauvola=img.clone();
    //std::cout<<"hi"<<std::endl;
  for(int i=w/2;i<img.rows-w/2;i++){
    for(int j=w/2;j<img.cols-w/2;j++){
      long long int sum=0,sq_sum=0;
      for(int m=-w/2;m<=w/2;m++){
        for(int l=-w/2;l<=w/2;l++){
          int val=(int)img.at<uchar>(i+m,j+l);
          sum+=val;
          sq_sum+=(val*val);
        }
      }
      float mean=sum/(1.0*w*w);
      float var=sq_sum/(1.0*w*w);
      float dev=var-(mean*mean);
      float sq_var=sqrt(dev);
      float temp_thresh=sq_var/128; //max std dev for grayscale 8U is 128.
      float thresh=mean*(1+k*(temp_thresh-1)); //sauvola formula.
      if((int)img.at<uchar>(i,j)>=thresh)sauvola.at<uchar>(i,j)=255;
      else sauvola.at<uchar>(i,j)=0;
    }
  }
  cv::Mat final_res=sauvola(cv::Rect(w/2,w/2,img.cols-w,img.rows-w));
  return final_res;
}

double compute_skew(cv::Mat src)
{
   // Load in grayscale.
   //cv::Mat src = cv::imread(filename, 0);
   cv::Size size = src.size();
   cv::bitwise_not(src, src);
   //cv::imshow("src",src);
   std::vector<cv::Vec4i> lines;
   //cv::HoughLinesP(src, lines, 1, CV_PI/180, 100, size.width / 2.f,20);
   cv::HoughLinesP(src, lines, 1, CV_PI/180, 100,size.width/5,20);
   cv::Mat disp_lines(size, CV_8UC1, cv::Scalar(0, 0, 0));
    double angle = 0.;
    unsigned nb_lines = lines.size();
    for (unsigned i = 0; i < nb_lines; ++i)
    {
        cv::line(disp_lines, cv::Point(lines[i][0], lines[i][1]),
                 cv::Point(lines[i][2], lines[i][3]), cv::Scalar(255, 0 ,0));
        double t_angle = atan2((double)lines[i][3] - lines[i][1],
                       (double)lines[i][2] - lines[i][0]);
        //std::cout<<t_angle<<" ";
        angle+=t_angle;
    }
    angle /= nb_lines; // mean angle, in radians.
  //cv::imshow("lines",disp_lines);
  //cv::waitKey(0);
    return (angle*180/CV_PI);

}
cv::Mat deskew(cv::Mat img, double angle)
{
  //cv::Mat img = cv::imread(filename, 0);

  cv::bitwise_not(img, img);

  std::vector<cv::Point> points;
  cv::Mat_<uchar>::iterator it = img.begin<uchar>();
  cv::Mat_<uchar>::iterator end = img.end<uchar>();
  for (; it != end; ++it)
    if (*it)
      points.push_back(it.pos());

  cv::RotatedRect box = cv::minAreaRect(cv::Mat(points));
  cv::Mat rot_mat = cv::getRotationMatrix2D(box.center, angle, 1);

  cv::Mat top_left = cv::Mat(3,1,CV_64FC1);
  top_left.at<double>(0,0)=0;top_left.at<double>(1,0)=0;top_left.at<double>(2,0)=1;
  cv::Mat new_top_left = rot_mat*top_left;
  //std::cout<<new_top_left.rows<<std::endl<<new_top_left.at<float>(0,0)<<std::endl<<new_top_left.at<float>(0,1)<<std::endl;
  //std::cout<<"new_top_left= "<<std::endl<<new_top_left<<std::endl;
  cv::Mat top_right = cv::Mat(3,1,CV_64FC1);
  top_right.at<double>(0,0)=0;top_right.at<double>(1,0)=img.cols;top_left.at<double>(2,0)=1;
  cv::Mat new_top_right = rot_mat*top_right;
  cv::Mat bottom_left = cv::Mat(3,1,CV_64FC1);
  bottom_left.at<double>(0,0)=img.rows;bottom_left.at<double>(1,0)=0;bottom_left.at<double>(2,0)=1;
  cv::Mat new_bottom_left = rot_mat*bottom_left;
  cv::Mat bottom_right = cv::Mat(3,1,CV_64FC1);
  bottom_right.at<double>(0,0)=img.rows;bottom_right.at<double>(1,0)=img.cols;bottom_right.at<double>(2,0)=1;
  cv::Mat new_bottom_right = rot_mat*bottom_right;

  cv::Point tl,br;
  if(new_top_left.at<double>(0,0)<new_top_right.at<double>(0,0))tl.y=new_top_right.at<double>(0,0);
  else tl.y=new_top_left.at<double>(0,0);
  if(new_top_left.at<double>(1,0)<new_bottom_left.at<double>(1,0))tl.x=new_bottom_left.at<double>(1,0);
  else tl.x=new_top_left.at<double>(1,0);
  if(new_bottom_left.at<double>(0,0)<new_bottom_right.at<double>(0,0))br.y=new_bottom_left.at<double>(0,0);
  else br.y=new_bottom_right.at<double>(0,0);
  if(new_bottom_right.at<double>(1,0)<new_top_right.at<double>(1,0))br.x=new_bottom_right.at<double>(1,0);
  else br.x=new_top_right.at<double>(1,0);


  cv::Mat rotated;
  cv::warpAffine(img, rotated, rot_mat, img.size(), cv::INTER_CUBIC);
  cv::Size box_size = box.size;
  if (box.angle < -45.)
    std::swap(box_size.width, box_size.height);
  cv::Mat cropped;
  cv::getRectSubPix(rotated, box_size, box.center, cropped);
  //std::cout<<tl.y<<" "<<tl.x<<" "<<br.y-tl.y<<" "<<br.x-tl.x<<std::endl;
  //std::cout<<cropped.rows<<" "<<cropped.cols<<std::endl;
  //cropped=cropped(cv::Rect(tl.x,tl.y,br.x-tl.x,br.y-tl.y));
  //cv::imshow("Original", img);
  //cv::imshow("Cropped", cropped);
  //cv::waitKey(0);
  return cropped;
}

cv::Mat simpleThreshold(cv::Mat im){
  for(int i=0;i<im.rows;i++){
    for(int j=0;j<im.cols;j++){
      if(im.at<uchar>(i,j)!=255)im.at<uchar>(i,j)=0;
    }
  }
  return im;
}
JNIEXPORT jint JNICALL Java_com_example_indicview_MainActivity_FindFeatures(JNIEnv* env, jobject jo, jlong addrGray,jint init,jint wordno,jlong retimg);

JNIEXPORT jint JNICALL Java_com_example_indicview_MainActivity_FindFeatures(JNIEnv* env, jobject jo, jlong addrGray,jint init,jint wordno,jlong retimg)
{
	int ini = init;
	Mat& toret =  *(Mat*)retimg;
	Mat& mRgb = *(Mat*)addrGray;
	if(ini)
	{
		cv::Mat gray=mRgb;
		cv::Mat bin=slow_sauvola_binarize(gray,15,0.1);
		  double angle =compute_skew(bin);
		  //std::cout<<angle<<std::endl;
		  cv::Mat image=deskew(bin,angle);
		image = simpleThreshold(image);
		image=dilate(image,1);
		mRgb=bin;
	}
  else
  {
    cv::Mat bin2=mRgb;
    int final_count=0;
    int i=0;
	cv::Mat bound=blob_joined(bin2,mRgb,i,&final_count,"k",wordno);
    toret=bound;
  }

    return retur;
}
}
