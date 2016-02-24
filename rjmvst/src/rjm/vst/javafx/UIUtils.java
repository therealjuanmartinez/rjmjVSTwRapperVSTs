package rjm.vst.javafx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class UIUtils {
	
    
	
	public static void showAlert(String title, String header, String content, Alert.AlertType alertType)
	{
		Platform.runLater(new Runnable() {                          
            @Override
            public void run() {
                try{
                    //FX Stuff done here
                	Alert alert = new Alert(alertType);
    				alert.setTitle(title);
    				alert.setHeaderText(header);
    				alert.setContentText(content);

    				alert.showAndWait();
                }finally{
                    //latch.countDown();
                }
            }
        });
	}
	
	
	public static void showAlert(String content)
	{
		Platform.runLater(new Runnable() {                          
            @Override
            public void run() {
                try{
                    //FX Stuff done here
                	Alert alert = new Alert(AlertType.INFORMATION);
    				alert.setTitle("");
    				alert.setHeaderText("");
    				alert.setContentText(content);

    				alert.showAndWait();
                }finally{
                    //latch.countDown();
                }
            }
        });
	}

}
