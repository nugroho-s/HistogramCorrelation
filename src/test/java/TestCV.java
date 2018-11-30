import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class TestCV {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void test(){
        Mat m = new Mat( 3, 3, CvType.CV_8UC1);
        m.put(0,0,1,2,3,4,5,6,7,8,9);
        Mat mask = new Mat(3,3,CvType.CV_8UC1);
        mask.put(0,0,0,0,1,0,1,0,1,0,0);
        Mat res = new Mat();
        m.copyTo(res,mask);
        System.out.println(res.dump());
    }
}
