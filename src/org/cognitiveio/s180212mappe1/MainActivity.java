/*
 * Contact: http://github.com/flexd
 * http://flexd.mit-license.org/
The MIT License (MIT)
Copyright © 2013 Kristoffer Berdal <web@flexd.net>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package org.cognitiveio.s180212mappe1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import org.cognitiveio.s180212mappe1.PopupDialog.DialogClickListener;

import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("DefaultLocale") // The default locale will _always_ be correct in this case, so there is no need to listen to the warnings :D
public class MainActivity extends Activity implements DialogClickListener{	
	

	// Defines the different states possible. FINISH is currently not used.
	public enum HangmanState {
		NOOSE, HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, DEAD, FINISH
	}
	
	// The possible words to use, loaded from XML resource.
	ArrayList<String> asWords;
	// The current target word.
	String sCurrentWord;
	// The current target word, split into an array. We do not really need the word in any other form.
	String[] saCurrentWordArray;
	// The hangman image, this shows the user the noose & man.
	ImageView  iwHangmanImage;
	// Our game state.
	HangmanState hsState;
	// The current word TextView and the wrong letter view.
	TextView twWordView;
	TextView twWrongLettersView;
	
	// Lists to handle guessed letters, wrong letters, the visible guessed word (eg. H_LLO) and the disabled button IDs.
	ArrayList<String> alGuessedLetters = new ArrayList<String>();
	ArrayList<String> alWrongLetters = new ArrayList<String>();
	ArrayList<String> alVisibleGuess  = new ArrayList<String>();
	ArrayList<Integer> alDisabledButtons = new ArrayList<Integer>();
	
	// Sound pool to play finish sounds.
	private SoundPool spSoundPool;
	private int iSoundID;
	boolean bSoundLoaded = false;
	// Getting the user sound settings
	SharedPreferences sharedPref;
	// Remaining letters, total wins and total losses.
	int iRemaining;
	int iTotalWins = 0;
	int iTotalLosses = 0;
	
	// Game running boolean and current locale.
	private boolean bGameRunning = false;
	private String sLocale;
	
	
	// Save our instance in case of power loss, killed because we are out of RAM and stuff like that.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList("guessedLetters", alGuessedLetters);
		outState.putStringArrayList("wrongLetters", alWrongLetters);
		outState.putStringArrayList("visibleGuess", alVisibleGuess);
		outState.putStringArrayList("words", asWords);
		outState.putIntegerArrayList("disabledButtons", alDisabledButtons);
		outState.putInt("remaining", iRemaining);
		outState.putInt("totalWins", iTotalWins);
		outState.putInt("totalLosses", iTotalLosses);
		outState.putString("currentWord", sCurrentWord);
		outState.putString("locale",  sLocale);
		outState.putSerializable("state", hsState);
		outState.putBoolean("gamerunning", bGameRunning);
		Log.d("WOOO", "Saving state!");
	}
	
	// Resumes the game from a saved bundle.
	private void resumeGame(Bundle saved) {
		alGuessedLetters  = saved.getStringArrayList("guessedLetters");
		
		alWrongLetters    = saved.getStringArrayList("wrongLetters");
		updateWrongLetterView(alWrongLetters);
		
		alVisibleGuess    = saved.getStringArrayList("visibleGuess");
		updateVisibleGuessView(alVisibleGuess); // I hope this works.
		
		asWords 		  = saved.getStringArrayList("words");
		
		alDisabledButtons = saved.getIntegerArrayList("disabledButtons"); // TODO: DISABLE BUTTONS?
		
		iRemaining		  = saved.getInt("remaining");
		iTotalWins		  = saved.getInt("totalWins");
		iTotalLosses	  = saved.getInt("totalLosses");

		sCurrentWord	  = saved.getString("currentWord");
		sLocale	  		  = saved.getString("locale");
		hsState			  = (HangmanState) saved.getSerializable("state");
		updateHangman(hsState);
		
		
	}
	
	// Creates the game.
    @Override
    protected void onCreate(Bundle savedBundleState) {
        super.onCreate(savedBundleState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        Log.d("LOCALE", "sLocale is: " + sLocale + " system locale is: " + Locale.getDefault());
        if (sLocale == null) {
        	sLocale = Locale.getDefault().toString();
        }
        else if (sLocale != Locale.getDefault().toString()) {
        	Log.d("LOCALE", "Locale has changed, new: " + Locale.getDefault().toString() + ", old: " + sLocale);
        	sLocale = Locale.getDefault().toString();
        }
        if (!sLocale.equals("nb_NO")) { // If we get here, sLocale is always defined.
        	Log.d("LOCALE", "X"+sLocale+"X, removing buttons");
        	
        	Button aa = (Button)findViewById(R.id.aa);
        	Button ae = (Button)findViewById(R.id.ae);
        	Button oe = (Button)findViewById(R.id.oe);
        	// Hide _ALL_ the buttons!
        	aa.setVisibility(View.GONE);
        	ae.setVisibility(View.GONE);
        	oe.setVisibility(View.GONE);
        	
        }
       
        
        iwHangmanImage = (ImageView) findViewById(R.id.hangmanView);
        twWordView = (TextView) findViewById(R.id.txtGuessedWord);
        twWrongLettersView = (TextView) findViewById(R.id.txtWrongLetters);
       
        // Set the font
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/PWScritch.ttf");
        twWordView.setTypeface(font);
        twWrongLettersView.setTypeface(font);
        
        // Grab the volume controls for music
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // Load the sound
        spSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        spSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
          @Override
          public void onLoadComplete(SoundPool soundPool, int sampleId,
              int status) {
        	  bSoundLoaded = true;
          }
        });
        
        // Load the sound
        iSoundID = spSoundPool.load(this, R.raw.cackle_end, 1);
       
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Portrait only.
        
        
        // Handle saved states, is the game running?
        if (savedBundleState != null) {
        	bGameRunning = savedBundleState.getBoolean("gamerunning");
        }
        // The game IS running, lets restore.
        if (bGameRunning && savedBundleState != null) {
        	resumeGame(savedBundleState);
        	Log.d("WOOO", "Resuming game!");
        }
        // The game was not running, start fresh.
        else {
        	 String[] temp = getResources().getStringArray(R.array.wordArray); // Fetch the words and start a new one!
             asWords = new ArrayList<String>(Arrays.asList(temp));
             newGame();
             Log.d("WOOO", "Starting new game!");
        }
    }
   
    // Handles the button presses on the "keyboard" buttons.
    public void handleKeyPressed(View v) {
    	Button b = (Button) v;
    	b.setEnabled(false);
    	alDisabledButtons.add(Integer.valueOf(b.getId()));
    	String guessedLetter = b.getText().toString();
    	boolean alreadyGuessed = false;
    	if  (hsState != HangmanState.DEAD) { 
	     	for (String s : alGuessedLetters) {
	    		if (s.equals(guessedLetter)) {
	    			alreadyGuessed = true;
	    			break;
	    		}
			}
	    	if (!alreadyGuessed) {
	    		
	    		alGuessedLetters.add(guessedLetter);
	    		if (guess(guessedLetter)) {
	        		// YAY CORRECT, TOAST?
	        		
	        		Log.d("COOLIO", "remaining is: " + iRemaining);
	        		if (iRemaining == 0) {
	        			Log.i("SILLY", "You've won silly!");
	        			setState(HangmanState.FINISH);
	        			iTotalWins++;
	        			String msg = String.format(getResources().getString(R.string.you_won_message), sCurrentWord, iTotalWins, iTotalLosses);
	        			
	        			DialogFragment	dialog= PopupDialog.newInstance(R.string.you_won, msg);	
	 					
						dialog.show(getFragmentManager(),"WinDialog");	
						bGameRunning = false; // We've won, no game running.

	        		}
	        		
	        	}
	        	else {
	        		// NOT CORRECT HAH.
	        		addWrongLetter(guessedLetter);
	        		nextState();
	        		Log.d("WRONG", "State is: " + hsState);
	        		if (hsState == HangmanState.DEAD) {
	        			Log.i("SILLY", "You've died silly!");
	        			iTotalLosses++;
	        			String msg = String.format(getResources().getString(R.string.you_lost_message), sCurrentWord, iTotalWins, iTotalLosses);
	       
	        			DialogFragment	dialog= PopupDialog.newInstance(R.string.you_died, msg);	
	 	
						dialog.show(getFragmentManager(),"DeathDialog");
						bGameRunning = false; // We're dead, game is done.
						
			    		boolean sound = sharedPref.getBoolean("hangman_sound_preference", true);
			   
			    		// Play the death sound, if the sound was loaded.
			    		if (sound) {
			    	      AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			    	      float actualVolume = (float) audioManager
			    	          .getStreamVolume(AudioManager.STREAM_MUSIC);
			    	      float maxVolume = (float) audioManager
			    	          .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			    	      float volume = actualVolume / maxVolume;
			    	      // Is the sound loaded already?
			    	      if (bSoundLoaded) {
			    	        spSoundPool.play(iSoundID, volume, volume, 1, 0, 1f);
			    	        Log.e("Test", "Played sound");
			    	      }
			    		}
	        		}
	        	}
	    	}
	    	else {
	    		// COMPLAIN LOUDLY. We can't actually get to this condition because the buttons are disabled when you use them.
	    		Log.i("SILLY", "You've already tried this letter, IMPOSSIBLE");
	    	}
    	}
    	else {
    		newGame();
    	}
    }
    // Adds a letter to the wrong letter arraylist.
    private void addWrongLetter(String letter) {
    	alWrongLetters.add(letter.toUpperCase());
		updateWrongLetterView(alWrongLetters);
    }
    // Updates the wrong letter view based on the ArrayList of characters.
    private void updateWrongLetterView(ArrayList<String> wl) {
    	twWrongLettersView.setText(arrayListToString(wl));
    }
    
    // Clears the wrong letter list and view.
    private void clearWrongLetters() {
    	alWrongLetters.clear();
    	twWrongLettersView.setText("");
    }
    // Sets the visible guessed word (e.g H_LLO) based on the arraylist.
    private void updateVisibleGuessView(ArrayList<String> vg) {
    	twWordView.setText(arrayListToString(vg));
    }
    // Converts an arraylist to a spaced out string like this: "H E L L O", or "H _ L L O"
    private String arrayListToString(ArrayList<String> list) {
    	StringBuffer buff = new StringBuffer();
    	for (String str : list) {
			buff.append(" "  + str);
		}
    	return buff.toString();
    	
    }
    
    // currently unused, used to repeat a string, eg. "repeat("F", 5) would return "FFFFF".
    private String repeat(String s, int repeat) {
    	StringBuilder sb = new StringBuilder(s.length() * repeat); 
    	for(int i = 0; i < repeat; i++) sb.append(s);
    	return sb.toString();
    }
    // Starts a entirely new game.
    private void newGame() {
	 	alGuessedLetters.clear();
        alVisibleGuess.clear();
	 	clearWrongLetters();
        sCurrentWord = getNewWord();
	 	saCurrentWordArray = sCurrentWord.split("");
	 	
        setState(HangmanState.NOOSE);
        // Enable all the previously disabled buttons, just because.
        for (Integer i : alDisabledButtons) {
			Button b = (Button) findViewById(i);
			b.setEnabled(true);
		}
        bGameRunning = true;
}
   
    // Gets us a new random word from asWords and removes it (for this session) so that it won't be picked again.
    private String getNewWord() {
    	
    	int iTargetWordID = new Random().nextInt(asWords.size()-1);
        String sWord = asWords.get(iTargetWordID).toLowerCase();
       
        iRemaining = sWord.length();
        for (int i = 0; i < sWord.length(); i++) {
        	alVisibleGuess.add("_");
		}
        updateVisibleGuessView(alVisibleGuess);
        Log.d("STUFF", "WordID is: " + iTargetWordID + " Word is then: " + asWords.get(iTargetWordID) + " Length of aWords is: " + asWords.size());
        asWords.remove(iTargetWordID);
        
    	return sWord;
    }
   
    // Validates a players guess.
    // Returns true/false.
    private boolean guess(String letter) {
    	boolean bResult = false;
    	for (int i = 0; i < saCurrentWordArray.length; i++) {
			String l = saCurrentWordArray[i];
			//System.out.println("Fra array: " + l + " Input: " + letter);
			if (l.equals(letter.toLowerCase())) {
				bResult = true;
				alVisibleGuess.remove(i-1);
				alVisibleGuess.add(i-1, letter);
				iRemaining -= 1;
			}
		}
    	updateVisibleGuessView(alVisibleGuess);
    	return bResult;
    }
    // Hops us to the next state in line.
    // We could just have used a incrementing integer here to track state, but this is more visually pleasing. :-)
    private void nextState() {
    	switch (hsState) {
    	case NOOSE:
    		setState(HangmanState.HEAD);
    		break;
    	case HEAD:
    		setState(HangmanState.BODY);
    		break;
    	case BODY:
    		setState(HangmanState.LEFT_ARM);
    		break;
    	case LEFT_ARM:
    		setState(HangmanState.RIGHT_ARM);
    		break;
    	case RIGHT_ARM:
    		setState(HangmanState.LEFT_LEG);
    		break;
    	case LEFT_LEG:
    		setState(HangmanState.DEAD); // He is dead now!
    		break;
    	case FINISH: // Show stuff here?
    		setState(HangmanState.NOOSE);
    		break;
    	}
    }
    
    // Sets out state to s and updates the ImageView.
    private void setState(HangmanState s) {
    	hsState = s;
    	updateHangman(s);
    }
    /// Sets our image based on the state s.
    // This could be merged with nextState() but in an ideal world these should be separate.
    private void updateHangman(HangmanState s) {
    	Resources resources = getResources();
    	switch (s) {
    	case NOOSE:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_1));
    		break;
    	case HEAD:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_2));
    		break;
    	case BODY:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_3));
    		break;
    	case LEFT_ARM:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_4));
    		break;
    	case RIGHT_ARM:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_5));
    		break;
    	case LEFT_LEG:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_6));
    		break;
    	case DEAD:
    		iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_7));
    		break;
		default:
			iwHangmanImage.setImageDrawable(resources.getDrawable(R.drawable.hangman_1)); // Noose as default.
			break;
    	}
    	
    }

    
    // Handles positive or negative click from PopupDialog, aka. the win or lose dialog.
    @Override	
	public void	onYesClick() {	
	 	newGame();
	}	
	@Override	
	public void onNoClick() {	
		finish();
	}	
 
}
