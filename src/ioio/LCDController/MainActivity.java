package ioio.LCDController;

import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/* 
 * 
 *********************************************************************************************************
 *  LCD 16x2  |   RS   |    E   |   D0   |   D1   |   D2   |   D3   |   D4   |   D5   |   D6   |    D7   |
 *********************************************************************************************************
 * IOIO Board | Port 1 | Port 2 | Port 3 | Port 4 | Port 5 | Port 6 | Port 7 | Port 8 | Port 9 | Port 10 |
 *********************************************************************************************************
 * 
 */

public class MainActivity extends IOIOActivity {
	
	// Create object for widget
	private Button btnset, btnclear, btninit;
	private EditText etxt1,etxt2;
	
	// onCreate function that will be do first when application is startup
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("System","On Create");
		// Set format of image that will be use in this application
        getWindow().setFormat(PixelFormat.RGBA_8888);
        
		// Run application in fullscreen (no notification bar)
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Set application use layout from main.xml
		setContentView(R.layout.main);

		// Assigne object to widget 
		btninit = (Button) findViewById(R.id.btninit);
		btnclear = (Button) findViewById(R.id.btnclear);
		btnset = (Button) findViewById(R.id.btnset);
		etxt1 = (EditText) findViewById(R.id.etxt1);
		etxt2 = (EditText) findViewById(R.id.etxt2);
		
		SharedPreferences sp = getPreferences(MODE_PRIVATE);
		String txt1 = sp.getString("EditText1", "Type Here");
		String txt2 = sp.getString("EditText2", "Type Here");

		etxt1.setText(txt1);
		etxt1.setSelection(etxt1.getText().length());
		etxt2.setText(txt2);
	}
	
	// On resume function
	@Override
    public void onResume() {
        super.onResume();
	}
	
	// On pause function
	@Override
    public void onPause() {
        super.onPause();
    }
	
	// On destroy function
	@Override
    public void onDestroy() {
        super.onDestroy();
    }
	
	// On stop function
	@Override
    public void onStop() {
        super.onStop();
        // Temporary lvWarn value to storage for use in next time
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("EditText1", etxt1.getText().toString());
        editor.putString("EditText2", etxt2.getText().toString());
        editor.commit();
    }

	// This class is thread for ioio board
	// You can control ioio board through this class 
	class Looper extends BaseIOIOLooper {
		
		// Create object for assigned to output port 
		private DigitalOutput D0,D1,D2,D3,D4,D5,D6,D7,RS,E;

		// This function will do when application is startup 
		// Like onCreate function but use with ioio board
		@Override
		public void setup() throws ConnectionLostException {
			
			// Assigned eacth object to each output port and initial state is false

			D0 = ioio_.openDigitalOutput(3, false);
			D1 = ioio_.openDigitalOutput(4, false);
			D2 = ioio_.openDigitalOutput(5, false);
			D3 = ioio_.openDigitalOutput(6, false);
			D4 = ioio_.openDigitalOutput(7, false);
			D5 = ioio_.openDigitalOutput(8, false);
			D6 = ioio_.openDigitalOutput(9, false);
			D7 = ioio_.openDigitalOutput(10, false);
			RS = ioio_.openDigitalOutput(1, false);
			E = ioio_.openDigitalOutput(2, false);
			
			// Create event when user selected this button widget
			// When user select this button LCD will clear screen 
			// and then will show text by depend on edit text
			btnset.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {  
					// Clear LCD screen
					lcd_command(0x01);
					
					// Send text in edit text to LCD screen
					Print(etxt1.getText().toString(),0x80);
					Print(etxt2.getText().toString(),0xC0);
				}
			});
			
			// Create event when user selected this button widget
			// When user select this button LCD will clear screen 
			btnclear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					lcd_command(0x01);
				}
			});

			// Create event when user selected this button widget
			// When user select this button LCD will initial 
			btninit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					lcd_init();
		            Print("LCD is Ready", 0x82);
				}
			});

			// if we use any command which not ioio command 
			// in any ioio board's function program will force close
			// then we could use runnable to avoid force close
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// When device connected with ioio board 
					// Toast will show "Connected!"
					Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
				}		
			});
		}

		// This function will always running when device connect with ioio board
		// It use for control ioio board
		@Override
		public void loop() throws ConnectionLostException { }
		
		// Function for send high pulse to LCD 
		public void enable() {
            try {
            	
            	// Set e to be High
	            E.write(true);
	            
	            // Send high pulse for one millisecond
	            Thread.sleep(1);
	            
	            // Set back to Low 
				E.write(false);
			} catch (ConnectionLostException e) {
			} catch (InterruptedException e) { }
        }
		
		// Function for convert integer to boolean and send to data port on LCD
		public void senddatabit(int i) {
			// Call function for convert integer to boolean 
			// and set boolean logic to each port
			try {
				D0.write(check(i));
				D1.write(check(i >> 1));
				D2.write(check(i >> 2));
				D3.write(check(i >> 3));
				D4.write(check(i >> 4));
				D5.write(check(i >> 5));
				D6.write(check(i >> 6));
				D7.write(check(i >> 7));
			} catch (ConnectionLostException e) {
				e.printStackTrace();
			}        
			
			// Call enable function 
            enable();
        }

		// Function for convert interger value to boolean
		public boolean check(int i) {
			
			// Create variable for convert binary to boolean
			// Use for command LCD on IOIO Board
			boolean st = false;
			i = i & 0x01;
			// If i = 0 set st = false or if i =1 set st = true
			// and return st back to main program
			if(i == 0x00)
				st = false;
			else if(i == 0x01)
				st = true;
			return st;
		}
		
		// Send command to LCD
		public void lcd_command(int com) {
            try {
            	// Set rs port to low 
				RS.write(false);
			} catch (ConnectionLostException e) {
				e.printStackTrace();
			}
            
            // Call senddatabit for send command
            senddatabit(com);
        }

		// Send command to LCD
		public void lcd_write(int text) {
            try {
            	// Set rs port to high 
				RS.write(true);
			} catch (ConnectionLostException e) {
				e.printStackTrace();
			}
            
            // Call senddatabit for send data
            senddatabit(text);
        }		
		
		// Send data to LCD
		public void lcd_init() {
			
			// LCD 8 Bit 5x7 Dot 2 Line
			lcd_command(0x38);
			
			// Clear screen
			lcd_command(0x01);
			
			// Display on, no cursor
			lcd_command(0x0C);  
			
			
        }
		
		// Send one letters to LCD with set address 
		public void SendC(char c, int address) {
			
			// Set address
            lcd_command(address);
            
            // Send the letters to LCD
            lcd_write(Integer.valueOf(c));
        }
		
		// Send text string to LCD
		public void Print(String str, int address) {
			
			// Send the letters one by one until the end
            for (int i = 0; i < str.length(); i++) {
                SendC(str.charAt(i), address);
                address++;
            }
        }
	}
	
	@Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }
}