package org.cognitiveio.s180212mappe1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class PopupDialog extends DialogFragment {
	private	DialogClickListener	callback;	

	public	interface	DialogClickListener	{	
		public	void	onYesClick();	
		public	void	onNoClick();	
	}

	@Override	
	public	void onCreate(Bundle savedInstanceState)	{	
		super.onCreate(savedInstanceState);	

		try	{	
			callback = (DialogClickListener)getActivity();	
		} catch	(ClassCastException	e)	{	
			throw	new	ClassCastException("Kallende klasse	må	implementere interfacet!");	
		}	
	}

	public static PopupDialog newInstance(int title, String message)	{	
		PopupDialog frag	=	new	PopupDialog();	
		Bundle	args	=	new	Bundle();	
		args.putInt("title", title);	
		args.putString("message", message);
		frag.setArguments(args);	
		return	frag;	
	}	@Override	
	public	Dialog	onCreateDialog(Bundle savedInstanceState)	{	

		int title = getArguments().getInt("title");
		return	new	AlertDialog.Builder(getActivity())	
		.setMessage(getArguments().getString("message"))
		.setTitle(title)	
		.setPositiveButton(R.string.new_game_button, new DialogInterface.OnClickListener()	{	
			public	void	onClick(DialogInterface	dialog,	int	whichButton)	{	
				callback.onYesClick();	
			}	
		})	
		.setNegativeButton(R.string.back_button, new DialogInterface.OnClickListener()	{	
				public	void onClick(DialogInterface dialog, int whichButton)	{	
					callback.onNoClick();	
				}	
			}).create();	
	}	
}
