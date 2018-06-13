package io.github.oliviercailloux.y2018.apartments.gui;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.oliviercailloux.y2018.apartments.apartment.Apartment;
import io.github.oliviercailloux.y2018.apartments.iconDisplay.DisplayIcon;
import io.github.oliviercailloux.y2018.apartments.toxmlproperties.XMLProperties;

/**
 * Class which enables the user to enter information about an apartment and to record it in a XML File (name given by the user in the constructor)
 * thanks to a GUI.
 */
public class CreateApartmentGUI {

	private Display display;
	private Shell shell;
	private Text title;
	private Text address;
	private Text floorArea;
	private Text nbBedrooms;
	private Text nbSleeping ;
	private Text nbBathrooms;
	private Button terrace;
	private Text floorAreaTerrace;
	private Text pricePerNight;
	private Text nbMinNight;
	private Text description;
	private Button wifi;
	private Button tele ;
	private File file;
	private Apartment apart;
	private final static Logger LOGGER = LoggerFactory.getLogger(CreateApartmentGUI.class);
	/**
	 * Initializes the constructor of the class.
	 * @param fileCompleteName String : name of the file the user wants to put Apartment information.
	 */
	public CreateApartmentGUI(String fileCompleteName) {
		this.display = new Display();
		this.shell = new Shell(display);
		this.file = new File(fileCompleteName);
		LOGGER.info("The GUI was initialized with success.");
	}

	/**
	 * General method which displays all the element of the GUI.
	 * @throws IOException if the logo doesn't load well.
	 */
	private void screenDisplay() throws IOException {
		try(InputStream f = DisplayIcon.class.getResourceAsStream("logo.png")){

			LOGGER.info("The logo has been loaded with success.");
			FillLayout r = new FillLayout();
			r.type = SWT.VERTICAL;
			shell.setLayout(r);
			Image i = new Image(display, f);
			shell.setImage(i);
			shell.setText("Apartments");


			createPageTitle();
			createForm();

			shell.pack();
			shell.setMinimumSize(400, 150);
			shell.setSize(600, 600);

			shell.open();
			LOGGER.info("The Shell was opened with success.");

			while(!shell.isDisposed( )){
				if(!display.readAndDispatch( ))
					display.sleep( );
			}
			i.dispose();
			display.dispose();
			LOGGER.info("The screen was closed with success.");
		}
	}
	
	/**
	 * Creates the form in adding different composites for inserting Apartment information.
	 */
	private void createForm() {
		title = createFormFieldComposite("Title of the apartment*: ");
		address = createFormFieldComposite("Address*: ");
		floorArea = createFormFieldComposite("Floor Area*:" );
		nbBedrooms = createFormFieldComposite("Number of bedrooms: ");
		nbSleeping = createFormFieldComposite("Sleeping capacity: ");
		nbBathrooms = createFormFieldComposite("Number of bathrooms: ");
		terrace = createCheckboxComposite("Terrace: ");
		floorAreaTerrace = createFormFieldComposite("Floor area terrace: ");
		pricePerNight = createFormFieldComposite("Price per night: ");
		nbMinNight = createFormFieldComposite("Minimum nights to stay: ");
		wifi = createCheckboxComposite("WiFi: ");
		tele = createCheckboxComposite("Television: ");
		description = createFormFieldComposite("Description:");


		terrace.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				Button b = (Button) e.widget;
				if (b.getSelection())
				{
					floorAreaTerrace.setBackground(new Color(display, 255,255,255));
					floorAreaTerrace.setEditable(true);
					floorAreaTerrace.setEnabled(true);
				}
				else
				{
					floorAreaTerrace.setEditable(false);
					floorAreaTerrace.setEnabled(false);
					floorAreaTerrace.setBackground(new Color(display, 200,200,200));
				}

			}


		});
		validationField();	
	}
	
	/**
	 * validationREquiredField initialize the listener for every component (Checkbox and Text) and execute the informationToFile routine in order to verify 
	 * if every textField respect its own format.
	 */
	private void validationField()
	{
		Listener textVerification = new Listener() {

			@Override
			public void handleEvent(Event e) {
				Text t = (Text)e.widget;
				if(!t.getText().isEmpty()){
					t.setBackground(new Color(display,255,255,255));
				}
				else
				{
					t.setBackground(new Color(display, 255,200,200));
				}
				informationToFile();
			}
		};
		
		Listener selectionListener =new Listener()
				{
					@Override
					public void handleEvent(Event arg0) {
						informationToFile();
					}
				};
		title.addListener(SWT.KeyUp,textVerification);
		address.addListener(SWT.KeyUp, textVerification);
		floorArea.addListener(SWT.KeyUp, textVerification);
		floorAreaTerrace.addListener(SWT.KeyUp, textVerification);
		terrace.addListener(SWT.Selection, selectionListener);
		nbBedrooms.addListener(SWT.KeyUp, textVerification);
		nbSleeping.addListener(SWT.KeyUp, textVerification);
		nbBathrooms.addListener(SWT.KeyUp, textVerification);
		pricePerNight.addListener(SWT.KeyUp, textVerification);
		nbMinNight.addListener(SWT.KeyUp, textVerification);
		wifi.addListener(SWT.Selection, selectionListener);
		tele.addListener(SWT.Selection, selectionListener);
		description.addListener(SWT.KeyUp, textVerification);
		informationToFile();
	}

	/**
	 * informationToFile is a method which verifies first the required fields first before writing into the xml file.
	 * In a second hand, it gets the checkbox status and verify every text field who must have a integer or a decimal number.
	 * If the user doesn't respect the format of a field, it won't be written into the file, the field is clear and highlight in red.
	 */
	private void informationToFile()
	{
		

		if(verificationText(floorArea, TypeButtonText.DOUBLE) && verificationText(floorArea, TypeButtonText.REQUIRED)
				&& verificationText(title, TypeButtonText.REQUIRED) && verificationText(address, TypeButtonText.REQUIRED) )
		{
			
			apart = new Apartment(Double.parseDouble(floorArea.getText()),address.getText(),title.getText());
			
			
			apart.setTele(tele.getSelection());
			apart.setWifi(wifi.getSelection());
			apart.setDescription(description.getText());

			if(verificationText(nbBedrooms, TypeButtonText.INT))
				apart.setNbBedrooms(Integer.parseInt(nbBedrooms.getText()));
			if(verificationText(nbMinNight, TypeButtonText.INT))
				apart.setNbMinNight(Integer.parseInt(nbMinNight.getText()));
			if(verificationText(nbSleeping, TypeButtonText.INT))
				apart.setNbSleeping(Integer.parseInt(nbSleeping.getText()));
			if(verificationText(nbBathrooms, TypeButtonText.INT))
				apart.setNbBathrooms(Integer.parseInt(nbBathrooms.getText()));
			if(verificationText(pricePerNight, TypeButtonText.DOUBLE))
			{
				apart.setPricePerNight(Double.parseDouble(pricePerNight.getText()));
			}
			
			if(terrace.getSelection() && verificationText(floorAreaTerrace, TypeButtonText.DOUBLE))
			{
					apart.setTerrace(terrace.getSelection());
					apart.setFloorAreaTerrace(Double.parseDouble(floorAreaTerrace.getText()));
			} 
			else
			{
				apart.setTerrace(false);
			}
			
			
			write(apart);
		}
	}



	/**
	 * Creates a composite (in the GUI) which contains a Text box.
	 * @param label String which corresponds to the the title of the Text box.
	 * @return Text containing the information of the Text box.
	 */
	private Text createFormFieldComposite(String label)
	{
		Composite c = new Composite(shell, SWT.PUSH);

		GridLayout f = new GridLayout(2, false);
		c.setLayout(f);
		GridData a = new GridData(SWT.FILL, SWT.CENTER, true, false);
		a.minimumWidth = SWT.FILL;
		a.horizontalAlignment = SWT.CENTER;
		a.widthHint = 200;
		Label lb = new Label(c, SWT.FILL);
		lb.setText(label);
		lb.setLayoutData(a);
		Text t = new Text(c, SWT.FILL);
		t.setText("");
		t.setLayoutData(a);

		if(label.equalsIgnoreCase("Floor area terrace: "))
			c.setEnabled(false);

			shell.pack();
		LOGGER.info("The Composite "+label+" was created.");
		return t;
	}
	
	/**
	 * Verifies that the data in parameters are in the good type. Change the background color of the Text Box if the type is not correct.
	 * @param text Text (data to analyze)
	 * @param type TypeButtonText (type required for the @param text)
	 * @return a boolean (true if the type is correct)
	 */
	private boolean verificationText(Text text, TypeButtonText type)
	{
		Color alertColor = new Color(display, 255,200,200);
		Color normalColor = new Color(display, 255,255,255);

		switch(type)
		{
		case INT :
			if(!text.getText().equals(""))
			{
				try {
					Integer.parseInt(text.getText());
					text.setBackground(normalColor);
					return true;
				}
				catch(NumberFormatException e){

					text.setText("");
					text.setBackground(alertColor);
					LOGGER.error("The argument set is not valid "+e.getMessage());
				}
			}
			break;
		case DOUBLE :
			if(!text.getText().equals(""))
			{
				try {
					Double.parseDouble(text.getText());
					text.setBackground(normalColor);
					return true;
				}
				catch(NumberFormatException e){
					text.setText("");
					text.setBackground(alertColor);
					LOGGER.error("The argument set is not valid "+e.getMessage());
				}
			}
			break;
		case REQUIRED :
			if(!text.getText().isEmpty())
			{
				text.setBackground(normalColor);
				return true;
			}
			text.setBackground(alertColor);
			break;
		default:
			break;
		}

		return false;
	}
	
	/**
	 * Creates a composite (in the GUI) which contains a check box.
	 * @param label String which corresponds to the the title of the check box.
	 * @return Button for the validation of data.
	 */
	private Button createCheckboxComposite(String label)
	{
		Composite c = new Composite(shell, SWT.PUSH);
		GridLayout f = new GridLayout(2, false);
		c.setLayout(f);
		GridData a = new GridData(SWT.FILL, SWT.CENTER, true, false);
		a.minimumWidth = SWT.FILL;
		a.horizontalAlignment = SWT.CENTER;
		a.widthHint = 200;
		Label lb = new Label(c, SWT.FILL);
		lb.setText(label);
		lb.setLayoutData(a);
		Button t = new Button(c, SWT.CHECK);
		t.setText("");
		t.setLayoutData(a);

		shell.pack();
		LOGGER.info("The Composite "+label+" was created.");
		return t;
	}


	/**
	 * Insert Apartment data inserted in the GUI, in a XML File. The name of the file is in the parameters of the class.
	 * @param a Object Apartment
	 */
	private void write(Apartment a) {
		XMLProperties xmlFile = new XMLProperties();
		try(FileOutputStream s = new FileOutputStream(file.getAbsolutePath()))
		{
			xmlFile.toXML(a, s);
		}
		catch (Exception e) {
			MessageDialog.openError(shell, "Error","Insertion Problem in the XML File\n\nTry to restart the app");
			LOGGER.error("Error while inserting data into XML File"+e.getMessage());
			throw new IllegalStateException(e);
		}

	}

	/**
	 * Initializes the top title of the GUI instance.
	 */
	private void createPageTitle() {
		Composite compoForTitle = new Composite(shell, SWT.CENTER);
		GridLayout gl = new GridLayout(1, true);
		compoForTitle.setLayout(gl);
		Label titleLb = new Label(compoForTitle, SWT.FILL | SWT.CENTER); 
		titleLb.setText("CREATE AN APARTMENT");
		GridData a = new GridData(SWT.FILL, SWT.CENTER, true, false);
		a.minimumWidth = SWT.FILL;
		a.horizontalAlignment = SWT.CENTER;
		a.widthHint = 200;
		titleLb.setLayoutData(a);
		LOGGER.info("The Composite of the header was created.");
	}
	/**
	 * 
	 * @param args
	 * 	must contains as first parameter the complete name of the file (Full Path).
	 * @throws IOException
	 */
	static public void main(String args[]) throws IOException {
		CreateApartmentGUI c = new CreateApartmentGUI(args[0]);
		c.screenDisplay();
	}
}
