package com.htfyun.model;

//import java.lang.reflect.Method;

/*
 * 重要声明： 这个 JAVA 文件对应的 JNI 功能实现里面，没有考虑
 * 多任务的情况，需要应用注意，不要出现并发调用的情况。
 */
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Context;

public class ModelDevice {
	private static final String TAG = "f2_jni";

	//======NOT CHANGED BEGIN!!=============
	// UART 	
	/* 以下参数用于 UartOpen ：
	 * UART_DEFAULT:表示使用原来的默认值， 波特率，校验，流控 都可以使用
	 *  这个默认值来进行设置。波特率默认是 1200，校验默认是 NONE ,流控默认是
	 *  NONE ,串口 库里面已经定死了，是  ttyS1 ,系统预留的串口就是这个。
	 *  所有函数，返回值 < 0 表示错误。
	 *  串口 调用 UartOpen 之后，可以进行 UartSendData UartRecvData UartClose 等操作。
	 *  UART 库里面使用非阻塞模式。如果 串口没有数据 ， UartRecvData 会返回 -1 ( read again ).
	 */
	public static final int UART_DEFAULT = -1;
	
	public static final int UART_PAR_NONE = 0;
	public static final int UART_PAR_ODD = 1;
	public static final int UART_PAR_EVEN = 2;
	
	public static final int UART_FC_NONE = 0;  
	public static final int UART_FC_CTSRTS = 1;  

	// volume 
	/*
	 * 以下参数用于设置音量： SetVolumes ， VOL_UNCHANGED 表示保持原来默认值。
	 * 比如我们只想调节 spk 的音量，可以使用 
	 * SetVolumes( XX , VOL_UNCHANGED , VOL_UNCHANGED );
	 * 该接口可以实现通话过程中动态调节各路音量。也可以在费通话情况下设置，
	 * 下次开始通话的时候就生效。
	 */
	public static final int VOL_UNCHANGED = -1;
	public static final int VOL_MIN = 0;
	public static final int VOL_MAX = 39;

	// GPIO.
	/*
	 * 以下参数用于设置 预留的GPIO 的配置： SetGpioConfig 。
	 * GPIO_DEFAULT 表示使用原来的默认值。但是第一个参数 : gpio 不能使用
	 * 默认值。系统可以配置的GPIO，可以通过接口  GpioGetInfo 返回。
	 * SetGpioConfig 配置GPIO的属性，包括输入，输出，是否要触发中断等。
	 * GetGpioValue 获取 GPIO 当前的 输入/输出 的值 （ 0 / 1 ).
	 */
	public static final int GPIO_DEFAULT = -1;
	
	public static final int GPIO_DIR_INPUT = 0;
	public static final int GPIO_DIR_OUTPUT = 1;

	// for input irq edge setting.
	public static final int GPIO_IRQE_NONE = 0;	// 不触发中断 
	public static final int GPIO_IRQE_FALLING = 1;  // 下降沿中断
	public static final int GPIO_IRQE_RISING = 2;   //  上升沿中断
	public static final int GPIO_IRQE_BOTHING = 3;  //  下降沿和上升沿 都中断。
	
	// gpio pin define.only define the fix using GPIO.
	public static final int GPIO_PIN_HANDSHANK_DET = 73; 	// 手柄提起/放下 检测。
	
	// gpio value

	// 20150906,fm1188 vol get/set
	public static final int FM1188_MIC_BOARD = 0;
	public static final int FM1188_MIC_HANDSHANK = 1;
	public static final int FM1188_MIC_MIX = 2;	// 20151126,MIX meens use HANDSHANK MIC and BOARD SPK.
	
	public static final int WORK_MODEL_NORMAL  		= 0;
	public static final int WORK_MODEL_FACTORYTEST  = 1;
	public static final int WORK_MODEL_SERVER  		= 2;
	//public static final String GPIO_STATE_FP = "/sys/devices/virtual/switch/rvgpio/state";
	
	// int *dir , int*value , int* irqe  , int* bit_mask
	private static final int GPIO_CNF_DIR_IND = 0;
	private static final int GPIO_CNF_VAL_IND = 1;
	private static final int GPIO_CNF_IRQE_IND = 2;
	private static final int GPIO_CNF_BITM_IND = 3;
	
	
	//======NOT CHANGED END!!=============
	
	public static final int MSG_GPIO_CHANGED = 1;
	
	// the file path to save/load config data.
	public static String configFilePathString = "/sdcard/f2_config.ini";
	//private View	mRecvCmdsShowView;
	private Handler mHandler=null;
	private static ModelDevice instance = null;
	private static int  mModelInitRet = -1;
	private Context mContext = null ;
	public static final int SPI_ERR_NOTINIT = -1;
	public static final int SPI_ERR_INVALID_PARAM = -2;
	
	private ModelDevice(){
	}
	
	/**
	 * get a ModelDevice instance.
	 * @param noCallback : true: the instance will not got GPio changed Event.
	 * @return
	 */
	public static ModelDevice getInstance( boolean noCallback ) {
		if(instance == null) {
			instance = new ModelDevice();
//			instance.SpiRecvMsgHandle(0, null, 0);
			/*
			 * ModelDeviceInitialize 的参数是 输入GPIO变化时的回调函数名称。
			 * 如果传入为空，则库函数就不会启动线程监测 GPIO 的值的变化。应用程序
			 * 可以通过  GetGpioValue 来获取当前 gpio 的值。
			 */
			String callbackFunction = "GpioInputChanged";
			if( noCallback ){
				callbackFunction = null;
			}
			mModelInitRet = instance.ModelDeviceInitialize(callbackFunction, configFilePathString);
			if( mModelInitRet < 0 ){
				instance = null;
			}
		}
		return instance;
	}
	
	public static ModelDevice getInstance() {
		return getInstance(false);
	}
	
	public static int bytesToInt(byte[] ary, int offset) {  
			int value;    
			value = (ary[offset]&0xFF)   
					| ((ary[offset+1]<<8) & 0xFF00)  
					| ((ary[offset+2]<<16)& 0xFF0000)   
					| ((ary[offset+3]<<24) & 0xFF000000);  
				return value;  
	}  
	
	/*
	 * 当一个设置为 输入，并且配置了中断的 GPIO 口，其输入电平发生变化的时候，
	 * 这个函数会被库里面调用。 gpio: 发生变化的 GPIO 口， 其值可以通过  GpioGetInfo 
	 * 函数返回的数组 获得 。 value: 该 GPIO 当前输入的电平，0： 低电平， 1： 高电平。
	 */
	private void GpioInputChanged(int gpio , int value )
	{
		Log.d(TAG, "enter GpioInputChanged,gpio=" + gpio +",value=" + value);
		if( mHandler != null) {
			Message msg = mHandler.obtainMessage();
//			msg.obj = gpio;
			msg.what = MSG_GPIO_CHANGED;
			msg.arg1 = gpio;
			msg.arg2 = value;
			mHandler.sendMessage(msg);
		}
	}
	
	public void ModelSetHandler(Handler h){
		mHandler = h;
	}
	
	/*
	 * 模块初始化流程： 
	 * 1. 先调用 getInstance() 初始化实例，获得指针；
	 * 2. 然后根据需要，通过 ModelSetHandler 和 ModelSetContext 设置 handler
	 * 	  和 context, 这两个不是必须的，主要是为了实现一些GPIO 发生改变时的
	 *   回调函数功能。
	 * 3. 之后就可以通过相关接口 进行 音量，串口，GPIO 口的控制。
	 * 4. 目前只要 打开 recorder ，系统就会配置上下行音频通路。如果同时打开播放音频通路，
	 *    就会有背景音乐合成到上行。
	 */
	public void ModelSetContext(Context context){
		mContext = context;
	}
	
	
	/*
	 * 保存当前模块的配置。当前模块的配置信息在 : /data/vol.ini 文件。可以 手工修改该文件，
	 * 系统在  ModelDeviceInitialize 的时候，会去加载这个文件里面的值。
	 * 这个文件的配置，如提供的例子所示。
	 */
	public int ModelConfigFlush(){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelConfigSave( configFilePathString );
		return ret;
	}
	
	/*
	 * 打开预留的串口，参数分别是 波特率，奇偶校验，流控设置。串口的其他设置库里面
	 * 固定了： bits = 8 , stopbit = 1. 可以通过修改配置文件来更改这些配置。
	 * 如果成功，返回 0 . 失败，返回负数。 文件句柄记录在库里面（不是返回值）。
	 */
	public int UartOpen(int bardrate , int parity , int flowctrl)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelUartOpen(bardrate,parity,flowctrl);
		return ret;
	}
	
	// 关闭打开的串口。关闭之后再执行 read/write 操作，将返回负数。
	public int UartClose(){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelUartClose();
		return ret;
	}
	
	/*
	 * 串口的写操作，参数分别是 byte 数据 以及有效数据长度。
	 * 返回值为实际写入的 字节。
	 */
	public int UartSendData(byte[] data , int len ){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelUartWrtie(data , len);
		return ret;
	}

	/*
	 * 串口度操作，返回值为实际读取的字节数。如果串口没有数据，
	 * 则返回 负数（-1 ）。
	 */
	public int UartRecvData(byte[] data, int len){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelUartRead(data , len);
		return ret;
	}

	public int UartRecvData(byte[] data){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelUartRead(data , data.length);
		return ret;
	}
	
	/**
	 * 设置音频音量的大小 ：
	 * drd_dl_vol ： 主面板下行（喇叭）音量的大小；
	 * brd_ul_vol :  主面板上行（MIC)音量的大小；
	 * hs_dl_vol ： 手柄下行（手柄喇叭）音量的大小；
	 * hs_ul_vol ： 手柄的上行（MIC)音量大小。
	 * 设置为 VOL_UNCHANGED 表示不修改这个音量原来的值。
	 */
	public int SetVolumes( int drd_dl_vol,int brd_ul_vol, 
		    int hs_dl_vol , int hs_ul_vol )
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelVolumeUpdate(drd_dl_vol,brd_ul_vol,hs_dl_vol,hs_ul_vol);
		return ret;
	}
	
	/*
	 * 获取当前设置的音量值，返回的音量在数组 volumes 里面：
	 * volumes[0]: drd_dl_vol
	 * volumes[1]: brd_ul_vol
	 * volumes[2]: hs_dl_vol
	 * volumes[3]: hs_ul_vol
	 */
	public int GetVolumes( byte[] volumes)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGetCurVolume( volumes , volumes.length );
		return ret;
	}
	
	/*
	 * 配置一个GPIO口的属性：
	 * gpio ： 要配置的 GPIO 口，通过  GpioGetInfo 返回。每一个 GPIO 口分别代表哪个管脚，
	 * 			需要应用自己定义。
	 * dir ： GPIO 的方向 -- 输入或者输出；
	 * value ： GPIO 的值，0：低电平，1：高电平。对于输出 GPIO ， 这个值忽略。
	 * irq_e ： GPIO 的中断触发配置。有不中断，下降沿，上升沿，下降上升沿。对于输出的 gpio，
	 * 			这个值忽略。
	 */
	public int SetGpioConfig(int gpio , int dir , int value ,int irq_e)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGpioSet(gpio,dir,value,irq_e);
		return ret;
	}
	
	/*
	 *  读取当前指定的 GPIO 口的值（ 可以是输入或者输出的 GPIO ）。
	 */
	public int GetGpioValue(int gpio)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int cnf[] = new int[4];
		int ret = ModelGpioGetConfig(gpio , cnf , cnf.length );
		if( ret < 0 )
			return ret;
		return cnf[GPIO_CNF_VAL_IND];
	}
	
	/*
	 *  读取当前指定的 GPIO 口的 DIR( 输入输出状态)。
	 *  返回值： < 0 :表示错误， GPIO_DIR_INPUT: 输入 ， 
	 *  GPIO_DIR_OUTPUT： 输出。
	 */
	public int GetGpioDir(int gpio)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int cnf[] = new int[4];
		int ret = ModelGpioGetConfig(gpio , cnf , cnf.length );
		if( ret < 0 )
			return ret;
		return cnf[GPIO_CNF_DIR_IND];
	}
	
	/*
	 *  读取当前指定的 GPIO 口的 irq edge setting( 中断设置状态 )。
	 *  返回值： < 0 :表示错误， 其他：
	 *  GPIO_IRQE_NONE -- GPIO_IRQE_BOTHING。
	 */
	public int GetGpioIrqEdge(int gpio)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int cnf[] = new int[4];
		int ret = ModelGpioGetConfig(gpio , cnf , cnf.length );
		if( ret < 0 )
			return ret;
		return cnf[GPIO_CNF_IRQE_IND];
	}
	
	/*
	 *  读取当前指定的 GPIO 口 config 信息，包括 dir,value,irq edge,bitmask.
	 */
	public int GetGpioConfigs(int gpio , int cnf[])
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGpioGetConfig(gpio , cnf , cnf.length );
		return ret;
	}
	
	// return the number of reserved gpio.and the gpio pin at array gpio.
	// return < 0 if error.
	public int GpioGetInfo(byte[] gpio ){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGpioGetInfo(gpio , gpio.length );
		return ret;
	}


	/*
	 * 获取fm1188 当前设置的音量值，返回的音量在数组 volumes 里面：
	 * volumes[0]: board mic vol
	 * volumes[1]: hand shank mic vol
	 * volumes[2]: linein mic vol 
	 */
	public int GetFm1188Volumes( int[] volumes)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGetFm1188Vols( volumes , volumes.length );
		return ret;
	}

	/*
	 * 设置fm1188 当前设置的音量值:
	 * mic: 0: board mic vol
	 *        1: hand shank mic vol
	 *        2: linein mic vol 
	 */
	public int SetFm1188Volumes( int mic , int vol )
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelSetFm1188Vol( mic , vol );
		return ret;
	}
	
	
	/*
	 * 获取fm1188 当前设置的输出 MIC( 上行 MIC ）。
	 * 返回值： < 0: 错误；
	 * 		   FM1188_MIC_BOARD ： 上行输入MIC 为主面板的 MIC.
	 * 		   FM1188_MIC_HANDSHANK ： 上行输入MIC 为手柄上面的MIC。
	 * 其他值： 也是错误.
	 */
	public int GetFm1188UplinkMic( )
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelGetFm1188InputMic( );
		if( ret != FM1188_MIC_BOARD && ret != FM1188_MIC_HANDSHANK ){
			ret = -1;
		}
		return ret;
	}

	/*
	 * 设置fm1188 当前 上行输入 MIC。可以根据手柄提起检测的 GPIO 信号或者用户需求进行设置。
	 * mic: 
	 * 		   FM1188_MIC_BOARD ： 上行输入MIC 为主面板的 MIC.
	 * 		   FM1188_MIC_HANDSHANK ： 上行输入MIC 为手柄上面的MIC。
	 * 		   FM1188_MIC_MIX: 上行输入为手柄的MIC,下行输出为 主面板 SPK。
	 * 	其他的值：错误。
	 */
	public int SetFm1188UplinkMic( int mic)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		if( mic != FM1188_MIC_BOARD && mic != FM1188_MIC_HANDSHANK &&
				mic != FM1188_MIC_MIX ){
			return SPI_ERR_INVALID_PARAM;
		}
		int ret = ModelSetFm1188InputMic( mic);
		return ret;
	}
	
	/*
	 * 开始进行通话，建立上下行通路
	 * start: 
	 * 		  1 ： 开始建立通话。
	 * 		  0 ： 结束通话。
	 *  其他的值：错误。
	 *  返回值： < 0 : 错误， 0： OK.
	 */
	public int StartCalling( int start)
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		if( start != 1 && start != 0 ){
			return SPI_ERR_INVALID_PARAM;
		}
		int ret = ModelStartCalling(start);
		return ret;
	}
	
	/*
	 * 应用程序退出的时候调用，反初始化。但是不能在 activity 的 onDestroy 里面调用。
	 * 因为这时候下层的进程还在运行。  -- 当前可以忽略这个函数！！
	 */
	public int ModelDeInit(){
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelDeInitialize();
		mModelInitRet = -1;
		return ret;
	}
	
	/*
	 * 进入设置模式。在设置模式下，上行的输入会在 codec 内部
	 * 同时输入到下行，用于工厂生产的时候验证外围器件贴片是否
	 * 有问题。必须在 start calling 之前调用。
	 * mode: 
	 * 		  WORK_MODEL_NORMAL , WORK_MODEL_FACTORYTEST
	 * 		  WORK_MODEL_SERVER
	 *  其他的值：错误。
	 *  返回值： < 0 : 错误， 0： OK.
	 */
	public int SetWorkMode( int mode )
	{
		if(mModelInitRet < 0 ) return SPI_ERR_NOTINIT;
		int ret = ModelSetTestMode(mode);
		return ret;
	}
	//=========native function=====================
	private native int ModelDeviceInitialize(String gpioChangeCallback , String config_fp );
	private native int ModelDeInitialize();
	
	private native int nativeSpiDestroy(int nativeObject );
	private native int ModelConfigSave( String config_fp );
	
	private native int ModelVolumeUpdate( int drd_dl_vol,int brd_ul_vol, 
		    int hs_dl_vol , int hs_ul_vol );
	
	private native int ModelUartOpen(int bardrate , int parity , int flowctrl);
	private native int ModelUartClose();
	private native int ModelUartWrtie(byte[] buffer , int buf_len );
	private native int ModelUartRead(byte[] buffer , int buf_len );
	
	private native int ModelGpioSet(int gpio , int dir , int value ,int irq_e);
	
	// the config order is: int *dir , int*value , int* irqe  , int* bit_mask
	private native int ModelGpioGetConfig(int gpio , int[] config , int len);
	private native int ModelGpioGetInfo(byte[] gpio , int len );
	private native int ModelGpioGetDir(int gpio);
	
	private native int ModelGetCurVolume(byte[] gpio , int len );

	private native int ModelGetFm1188Vols(int[] vols , int len );
	private native int ModelSetFm1188Vol(int  mic, int vol );
	
	private native int ModelGetFm1188InputMic();
	private native int ModelSetFm1188InputMic( int  mic );
	
	private native int ModelStartCalling( int start); 
	
	/*
	 * 20151226,HSL,add test mode for factory test!!
	 */
	private native int ModelSetTestMode( int  testMode );
	static {
		System.loadLibrary("f2_jni");
	}
}
