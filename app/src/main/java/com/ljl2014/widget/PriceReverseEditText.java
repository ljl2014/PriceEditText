package com.ljl2014.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

/**
* @author  兰健龙
* @phone   17688490717
* @email   306685645@qq.com
* @date 2016年2月24日 上午11:19:23 
* @version 1.0
* 要使用带分组的数字时，需要在代码中设置调用setDecimals()
* 注意在xml中不能再使用android:maxLength=""这个属性，当需要这个属性时，
* 用maxInteger+decimalPlace+konggeNumberB+1代替
* 
* OrderActivity中有使用例子
* 使用：1.在xml文件中设置相当属性即可
* 2. 在代码在动态调用方法
* edt_price.setDecimals();
* edt_price.setDecimalPlace(4);
edt_price.setWeightNum(3);
edt_price.setMaxInteger(9);
edt_price.setStrformat(' ');

获得字符串时，调用getStringText()即可，调用getText()时需要自已去除分隔的字符
* 
 */
public class PriceReverseEditText extends EditText implements
		View.OnFocusChangeListener{
	
	private TextChangedListener listener;
	/*
	 * 几位几组，默认为0
	 */
	private int weightNum;
	/*
	 * 目前只能识别第一位的字符，建议只设置一位，默认为空格
	 */
	private String strformat;//间隔字符串, 
	private int maxInteger;//最大整数位，正数，负数无效 
	private int decimalPlace;//保留小数点后几位
	
	int beforeTextLength = 0;
	int onTextLength = 0;
	boolean isChanged = false;

	int cursoLocation = 0;// 记录光标的位置
	private char[] tempChar;
	private static StringBuffer buffer = new StringBuffer();
	int konggeNumberB = 0;
	int konggeNumberC = 0;
	String beforText;
	private boolean isDecimals = true;
	public void setTextChangedListener(TextChangedListener listener) {
		this.listener = listener;
	}

	public interface TextClearListener {
		void onTextClear();
	}

	/**
	 * 删除按钮的引用
	 */
	private Drawable mClearDrawable;
	/**
	 * 控件是否有焦点
	 */
	private boolean hasFoucs;

	public PriceReverseEditText(Context context) {
		this(context, null);
	}
	public PriceReverseEditText(Context context, AttributeSet attrs) {
		// 这里构造方法也很重要，不加这个很多属性不能再XML里面定义
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public PriceReverseEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PriceEdit, defStyle, 0);  
		weightNum = a.getInt(R.styleable.PriceEdit_weightNum, 0);
		//最大整数位
		maxInteger = a.getInt(R.styleable.PriceEdit_maxInteger, 4);
		if(maxInteger < 4)maxInteger = 4;//默认4位
		
		//默认为" "
		strformat = a.getString(R.styleable.PriceEdit_strformat);
		if(null == strformat) strformat = " ";
		decimalPlace = a.getInt(R.styleable.PriceEdit_decimalPlace, 1);//默认1位小数点
		if(decimalPlace < 1) decimalPlace = 1;
	
//		setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxInteger + (weightNum == 0 ? 0 : maxInteger/weightNum) + decimalPlace + 1)});
		a.recycle();
		init();
	}

	private void init() {
		
		addTextChangedListener();
		// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
		mClearDrawable = getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			// throw new
			// NullPointerException("You can add drawableRight attribute in XML");
			mClearDrawable = getResources().getDrawable(R.drawable.edt_delete);
		}

		// //getIntrinsicWidth()取得的是Drawable在手机上的宽度，所以不同分辨率下获取到的值是不同的，关键所在处
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());

		// 默认设置隐藏图标
		setClearIconVisible(false);
		// 设置焦点改变的监听
		setOnFocusChangeListener(this);
		// 设置输入框里面内容发生改变的监听
//		addTextChangedListener(this);
	}
	private void addTextChangedListener(){
		addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (hasFoucs) {
					setClearIconVisible(s.length() > 0);
					if(isClearIcon){
						replaceEmojiStr(s.toString());
					}
					else if (isDecimals) {
						//输入0后，不输入小数点，无效
						if (s.toString().startsWith("0")
								&& s.toString().trim().length() > 1) {
							if (!s.toString().substring(1, 2).equals(".")) {
								if(s.length()>1){
									s = s.toString().subSequence(s.toString().indexOf("0")+1, s.length());
									boolean zero = true;
									for (int i = 0; i < s.length(); i++) {
										if(s.charAt(i) == strformat.charAt(0)){
											continue;
										}
										if(s.charAt(i) != '0'){
											zero = false;
											break;
										}
									}
									if(zero){//全部为0
										s = "";
										setText(s);
										setSelection(0);
									}else{
										if(s.toString().contains(".")){
											while ((s.toString().startsWith("0") ||s.toString().startsWith(strformat)) 
													&& s.toString().indexOf(".")!=1) {
												if(s.length() <= 1){
													s = "";
												}else{
													s = s.toString().substring(1);
												}
											}
										}else{
											while (s.toString().startsWith("0") ||s.toString().startsWith(strformat)) {
												if(s.length() <= 1){
													s = "";
												}else{
													s = s.toString().substring(1);
												}
											}
										}
										setText(s);
										if(beforText.startsWith("0")){
											setSelection(start-1 <= 0 ? 0 : start);
										}else{
											setSelection(start);
										}
									}
								}else{
									s = s.subSequence(0, 1);
									setText(s.subSequence(0, 1));
									setSelection(1);
								}
							}
						}
						//首位是小数点
						if (s.toString().startsWith(".")) {
							if(s.length()>1){
								s = s.toString().substring(s.toString().indexOf(".")+1, s.length());
								setText(s);
								setSelection(start);
							}else{
								s = "0" + s;
								setText(s);
								setSelection(2);
							}
						}
						
						buffer.append(s.toString());
						onTextLength = s.length();
						if (onTextLength == beforeTextLength || onTextLength <= (weightNum-1) || isChanged) {
							isChanged = false;
							return;
						}
						isChanged = true;

						int indexDot = s.toString().indexOf(".");
						if (s.toString().trim().contains(".")) {
							if(indexDot > (maxInteger + konggeNumberB)){//整数超,看小数
								String dotString = s.toString().substring(s.toString().indexOf("."));
								if(dotString.toString().contains(strformat)){//去字符
									dotString = dotString.toString().replaceAll(strformat, "");
								}
								int b = 0;
								if(dotString.length() > decimalPlace+1){//小数超
									dotString = beforText.substring(beforText.indexOf("."));
									dotString = dotString.substring(0, dotString.indexOf(".") + decimalPlace+1);
									s = s.toString().substring(0, s.toString().indexOf("."));
									if(s.toString().endsWith(strformat)){
										s = s.toString().trim();
										s = s.toString() + dotString;
										b = 1;
									}
									setText(s);
									setSelection(start-b);
								}else{//小数未超过
							    	String integer = beforText.toString().substring(0, maxInteger + konggeNumberB);
									s = integer + dotString;
									setText(s);
									setSelection(start);
								}
							}else{//整数不超，看小数位
								int b = 0;
								if (indexDot == (maxInteger + konggeNumberB) && s.length() - 1 - s.toString().indexOf(".") > decimalPlace) {//小数位超
									String dotString = beforText.substring(s.toString().indexOf("."));
									String integer = s.toString().substring(0, s.toString().indexOf("."));
									if(integer.endsWith(strformat) || s.length()-1-s.toString().indexOf(".") > decimalPlace+1){
										integer = integer.trim();
										b=1;
									}
									if(dotString.toString().contains(strformat)){//去字符
										dotString = dotString.toString().replaceAll(strformat,"");
									}
									if(dotString.length() >= decimalPlace + 1){
										dotString = dotString.substring(0, dotString.indexOf(".") + decimalPlace + 1);
										s = integer + dotString;
										setText(s);
										setSelection(start-b);
									}else{
									s = integer + dotString;
									setText(s);
									if(s.length() - start >= 2)
										setSelection(s.length() -1 -b);
									else
										setSelection(s.length());
									}
								}else{
									if(s.length() - 1 - s.toString().indexOf(".") > decimalPlace){
										String dotString = s.toString().substring(s.toString().indexOf("."));
										String integer = s.toString().substring(0, s.toString().indexOf("."));
										if(dotString.toString().contains(strformat)){//去字符
											dotString = dotString.toString().replaceAll(strformat,"");
										}
										if(dotString.length() > decimalPlace + 1){
											dotString = dotString.substring(0, dotString.indexOf(".") + decimalPlace + 1);
										}
										if(integer.endsWith(strformat)){
											integer = integer.trim();
											b = 1;
											s = integer + dotString;
											setText(s);
											if(weightNum <= 0) weightNum = 1;
											if(integer.length()/weightNum <= weightNum){
												setSelection(start);
											}
											else{
												setSelection(start + b > s.length() ? start : start+b);
											}
											return;
										}
										if(s.length()-1-s.toString().indexOf(".") >= decimalPlace+1){
											integer = integer.trim();
											b = 1;
											if(integer.length() <= weightNum){
												integer = integer.replaceAll(strformat, "");
											}
											s = integer + dotString;
											setText(s);
											setSelection(start + b > s.length() ? start : start+b);
											return;
										}
										
										
										s = integer + dotString;
										setText(s);
										if(s.length() - start >= 2){
											setSelection(s.length() -1 -b);
										}else
											setSelection(s.length());
									}
								}
							}
						}
						
						//=======================================
						if (s.length() > (maxInteger + konggeNumberB)) {
							int index = s.toString().indexOf(".");
							if (index > (maxInteger + konggeNumberB)) {
								String d = s.toString().substring(s.toString().indexOf("."));
								
								String temp = beforText.substring(0, maxInteger + konggeNumberB);
								s = temp + d;
								setText(s);
								setSelection(start);
							} 
							else if (index == -1) {
								s = s.toString().substring(0, (maxInteger + konggeNumberB));
								String temp = beforText.substring(0, maxInteger + konggeNumberB);
								s = temp;
								setText(s);
								setSelection(start);
							}
						}
						//=============================================
						
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				beforText = s.toString();
				beforeTextLength = s.length();
				if (buffer.length() > 0) {
					buffer.delete(0, buffer.length());
				}
				konggeNumberB = 0;
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) == strformat.charAt(0)) {
						konggeNumberB++;
					}
				}
				
			}
			@Override
			public void afterTextChanged(Editable s) {
				if (isChanged) {
					cursoLocation = getSelectionEnd();
					int index = 0;
					while (index < buffer.length()) {
						if (buffer.charAt(index) == strformat.charAt(0)) {
							buffer.deleteCharAt(index);
						} else {
							index++;
						}
					}
					
//					index = 0;
					konggeNumberC = 0;
					int length = 0;
					if(buffer.toString().contains(".")){
						length = buffer.toString().indexOf(".");
					}else{
						length = buffer.length();
					}
					int count = 0;
					for (int i = length-1; i > 0; i--) {
						count ++;
						if(weightNum != 0 && count % weightNum == 0){
							buffer.insert(i, strformat);
							konggeNumberC++;
						}
					}
					if (konggeNumberC > konggeNumberB) {
						cursoLocation += (konggeNumberC - konggeNumberB);
					}
					if(konggeNumberB > konggeNumberC)
						cursoLocation -= Math.abs(konggeNumberB - konggeNumberC);

					tempChar = new char[buffer.length()];
					buffer.getChars(0, buffer.length(), tempChar, 0);
					String str = buffer.toString();
					
					if(str.toString().contains(".")){
						while ((str.toString().startsWith("0") ||str.toString().startsWith(strformat)) 
								&& str.toString().indexOf(".")!=1) {
							if(str.length() <= 1){
								str = "";
							}else{
								str = str.toString().substring(1);
							}
						}
					}else{
						while (str.toString().startsWith("0") ||str.toString().startsWith(strformat)) {
							if(str.length() <= 1){
								str = "";
							}else{
								str = str.toString().substring(1);
							}
						}
					}
	
					
					if (cursoLocation > str.length()) {
						cursoLocation = str.length();
					} else if (cursoLocation < 0) {
						cursoLocation = 0;
					}
					
					setText(str);
					Editable etable = getText();
					Selection.setSelection(etable, cursoLocation);
					isChanged = false;
				}
				
				if (listener != null) {
					listener.onTextChanged(getId());
				}
			}
		});
	}
	int beforCursor;
	/**
	 * @Description: 对外提供设置小数点后几位
	 * @param decimalPlace   负数无效
	 * @return void  
	 * @throws
	 * @author 兰健龙
	 * @date 2016年2月25日
	 */
	public void setDecimalPlace(int decimalPlace){
		if(decimalPlace < 1)
			throw new RuntimeException(new IllegalArgumentException("Set to a number greater than 1"));
		
		this.decimalPlace = decimalPlace;
	}
	/**
	 * @Description: 设置数字位数分组，如4，表示4个数字一组，从最高位开始算起
	 * @param weightNum   
	 * @return void  
	 * @throws
	 * @author 兰健龙
	 * @date 2016年2月25日
	 */
	public void setWeightNum(int weightNum){
		if(weightNum < 0)
			throw new RuntimeException(new IllegalArgumentException("Set to a number greater than 0"));
		
		this.weightNum = weightNum;
	}
	/**
	 * @Description: 对外设置分隔的字符
	 * @param c   
	 * @return void  
	 * @throws
	 * @author 兰健龙
	 * @date 2016年2月25日
	 */
	public void setStrformat(char c){
		if(c == '.'){
			throw new RuntimeException(new IllegalArgumentException("Parameter illegal argument cannot be '.'"));
		}
		strformat = String.valueOf(c);
	}
	public String getStrformat(){
		return strformat;
	}
	/**
	 * @Description: 设置最大整数位数
	 * @param maxInteger   
	 * @return void  
	 * @throws
	 * @author 兰健龙
	 * @date 2016年2月25日
	 */
	public void setMaxInteger(int maxInteger){
		if(maxInteger < 4){
			throw new RuntimeException(new IllegalArgumentException("Set to a number greater than 4"));
		}
		this.maxInteger = maxInteger;
	}
	public String getStringText(){
		if(!TextUtils.isEmpty(getStrformat())){
			return getText().toString().replaceAll(getStrformat(), "");
		}
		return getText().toString();
	}
	/**
	 * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 当我们按下的位置 在 EditText的宽度 -
	 * 图标到控件右边的间距 - 图标的宽度 和 EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			if (getCompoundDrawables()[2] != null) {

				boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
						&& (event.getX() < ((getWidth() - getPaddingRight())));

				if (touchable) {
					this.setText("");
				}
			}
		}

		return super.onTouchEvent(event);
	}

	/**
	 * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		this.hasFoucs = hasFocus;
		if (hasFocus) {
			setClearIconVisible(getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * 
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? mClearDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
	}

	/** 设置为小数,只能输入数字，与与isClearIcon不能公用不能公用 **/
	public void setDecimals() {
		isDecimals = true;
		isClearIcon = false;
	}

	
	private String regex = "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]";
	private Pattern emoji = Pattern.compile (regex, Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE );
	private void replaceEmojiStr(String input){
		boolean isEmoji = false;
		Matcher emojiMatcher = emoji.matcher(input);
		if(emojiMatcher.find()){
			isEmoji = true;
		}
		if(isEmoji){
			input = emojiMatcher.replaceAll("");
			setText(input);
			setSelection(input.length());
		}
	}
	private boolean isClearIcon = false;

	/** 默认过滤输入中的表情符号（uinon字符）与setDecimals不能公用 **/
	public void setClearIcon(boolean isClearIcon) {
		this.isClearIcon = isClearIcon;
		if (isClearIcon) {
			isDecimals = false;
		}
	}
	
}