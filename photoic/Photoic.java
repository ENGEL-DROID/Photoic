/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package photoic;

import java.awt.Dimension;
import windows.NewLayer;
import LayerPackage.LayerContainer;
import LayerPackage.Layer;
import LayerPackage.LayerFactory;
import StatePackage.StateMove;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import LayerPackage.LayerStyle;
import StatePackage.StatePen;
import StatePackage.StateTempSave;
import UndoRedo.LayerShiftUndo;
import UndoRedo.LayerStateUndo;
import UndoRedo.MergeUndo;
import UndoRedo.MoveUndo;
import UndoRedo.ResizeUndo;
import UndoRedo.addLayerUndo;
import UndoRedo.deleteLayerUndo;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import static javafx.application.Application.launch;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import windows.FontChooser;
import windows.NewProject;
import windows.SaveProject;
import windows.StrokeSettings;
import projectManager.ProjectObject;
import projectManager.ZipFolder;
import windows.OpenProject;

/**
 *
 * @author ELCOT
 */


public class Photoic extends Application {
    public WorkPlace place;
    Pane root;
    static public ArrayList<LayerContainer> layerList;
    static public ArrayList<Layer> originalLayers;
    static public Stage newProjectwindow,openFilewindow;
    HBox adjustmentBar;
    Layer selected;  
    LayerFactory layerFactory;
    LayerStyle layerSettings;
    static ToggleGroup toolGroup;
    public static Button saveLossless;
    static public StrokeSettings strokeSet;
    static public ListView<String> layers;
    static public HBox fontChooser;
    static public NewProject newProject;
   
    @Override
    public void start(Stage primaryStage) {
        place=new WorkPlace(1000,600);
        place.p=this;
        layerList=new ArrayList<>();
        originalLayers=new ArrayList<>();
        layerFactory=new LayerFactory();
        strokeSet=new StrokeSettings();
        fontChooser=FontChooser.getFontList();
        layerSettings=new LayerStyle(place);
        newProject=new NewProject(this);
        
        setUI(primaryStage);
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
// try{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
       // catch(Exception e){
            //System.loadLibrary("opencv_java411");
            
       String mainPath=new File("").getAbsolutePath();    
       System.out.println("Main path "+mainPath);
       
     //  System.setProperty("java.library.path","C:\\Users\\ELCOT\\Documents\\NetBeansProjects\\Photoic\\src\\dll\\opencv_java4ll_64.dll");
      // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      // launch(args);    
      
      if(LoadLibrary.loadOpenCV(mainPath, args)){
            launch(args);
      }   
    }

    private void setUI(Stage primaryStage) {
        VBox main=new VBox();
        //placess
        
        //tool bar integration
        HBox toolBar=getToolBar();
        
        //menu integration
        MenuBar menuBar=getMenu();
        
        HBox  layerBox=new HBox();
        root=place.getPane();
        
        ScrollPane scroll=new ScrollPane();
        scroll.setMaxHeight(580);
        scroll.setMaxWidth(1000);
        scroll.setContent(root);
        
        layerBox.setSpacing(10);
        layers=getLayerlist();
        layerBox.getChildren().addAll(layers,scroll);
        main.getChildren().addAll(menuBar,toolBar,getActionButtons(),layerBox);
        main.setSpacing(10);
        
        Scene scene = new Scene(main);
        scene.setOnKeyPressed((KeyEvent event) -> {
            if(selected!=null)
            keyListenerMove(event);
        });
        saveLossless.setVisible(false);
        primaryStage.setTitle("Photoics");
        primaryStage.getIcons().add(new Image("Images/photoic.png"));
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    
    private ListView<String> getLayerlist(){
       layers= new ListView<String>();
       layers.setMaxSize(150, 700);
       ContextMenu multiMenu=new ContextMenu();
       ContextMenu menu=new ContextMenu();
       layers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
          @Override
           public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
               for(int i=0;i<place.layers.size();i++){
                 if(place.layers.get(i).getName()==null){System.out.println("nulll");}
                 try{
                 if(place.layers.get(i).name.equals(newValue)){
                 selected=place.layers.get(i);
                 place.selected=selected;
                 System.out.println("selected is.. "+selected.getName());
                 }}catch(Exception e){}
                 }
                 layers.setContextMenu(menu);
                 if(layers.getSelectionModel().getSelectedItems().size()>1){
                     
                    layers.setContextMenu(multiMenu);
                 }
               
           }
       });
       MenuItem merge=new MenuItem("Merge Layers");
       merge.setOnAction((ActionEvent event) -> {
           ObservableList<String> selectedItems = layers.getSelectionModel().getSelectedItems();
           ArrayList<Layer>toMerge=new ArrayList<>();
           for(int i=0;i<selectedItems.size();i++){
             for(int j=0;j<place.layers.size();j++){
             if(place.layers.get(j).getName().equals(selectedItems.get(i))){
             toMerge.add(place.layers.get(j));
             }
             }
           }
           System.out.println(toMerge);
           mergeAll(toMerge);
       });
       MenuItem copy=new MenuItem("Copy   (Ctrl+C)");
       copy.setOnAction((ActionEvent event) -> {
         Layer newL=addLayer(layerFactory.copy(selected),layerList.indexOf(getLayerContainer(selected.getName()))+1);
         Universal.addUndo(new addLayerUndo(place,this,newL));
        });
       MenuItem delete=new MenuItem("Delete");
       delete.setOnAction((ActionEvent event) -> {
           if(selected==null){System.out.println("nulll ");}
           else{
           Universal.addUndo(new deleteLayerUndo(place,this,selected,layerList.indexOf(selected.preview)));
           this.deleteLayer(selected);
           }
           
        });
       MenuItem moveup=new MenuItem("Shift up  (Ctrl+U)");
       moveup.setOnAction((ActionEvent event) -> {
           if(selected==null){System.out.println("nulll ");}
           else{
               int old=place.layers.indexOf(selected);   
               int ne=place.layers.indexOf(selected)-1;
               if(this.moveLayer(selected,old,ne)){
               Universal.addUndo(new LayerShiftUndo(place,this,selected,old,ne));
    }
           }
           
        });
       MenuItem movedown=new MenuItem("Shift Down  (Ctrl+D)");
       movedown.setOnAction((ActionEvent event) -> {
           if(selected==null){System.out.println("nulll ");}
           else{
               int old=place.layers.indexOf(selected);   
               int ne=place.layers.indexOf(selected)+1;
               if(this.moveLayer(selected,old,ne)){
               Universal.addUndo(new LayerShiftUndo(place,this,selected,old,ne));
               }
           }
        });
       MenuItem settings=new MenuItem("Settings");
       settings.setOnAction((ActionEvent event) -> {
           if(selected==null){System.out.println("nulll ");}
           else{
            layerSettings.launch(selected);
           }
        });
       menu.getItems().addAll(copy,delete,moveup,movedown,settings);
       multiMenu.getItems().addAll(merge);
       layers.setContextMenu(menu);
       layers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
       return layers;
    }
    
    private String getLayerContainer(String id){
     for(int i=0;i<place.layers.size();i++){
     if(place.layers.get(i).name.equals(id)){
        return place.layers.get(i).getPreview().getLayout();
     }
     }
     System.out.println("return nulllll");
     return null;
    }
    

    
    
    private MenuBar getMenu(){
    MenuBar menuBar=new MenuBar();
    
    Menu fileMenu=new Menu("File");
    Menu editMenu=new Menu("Edit");
    Menu imageMenu=new Menu("Image");   
    Menu helpMenu=new Menu("Help");
    
    MenuItem newFile=new MenuItem("New Project");
    MenuItem insert=new MenuItem("Insert Image..");
    MenuItem openFile=new MenuItem("Open Project");
    MenuItem saveFile=new MenuItem("Save");
    MenuItem saveasFile=new MenuItem("Save As");
    MenuItem quitFile=new MenuItem("Exit");
    
    newFile.setOnAction((ActionEvent event) -> {
        if(layers.getItems().size()>0){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation your saving");
        alert.setContentText("Do you want to save? /n If you already saved, press NO");
        ButtonType okButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(okButton, noButton);
        alert.showAndWait().ifPresent(type -> {
        if (type.getText().equals("Yes")) {
            SaveProject.launchSave(new ProjectObject(this,new Universal()));
        } else {
            newProject.launch();
        }
                                               });
        }
        else{
          newProject.launch();}
    });
    
    insert.setOnAction((ActionEvent event) -> {
        FileChooser open=new FileChooser();
        File selected=open.showOpenDialog(null);
        if(selected!=null){
        System.out.println(selected.toURI()+" "+selected.getName());
        Image image=null;
            try {
                image = new Image(selected.toURL().toString());
            } catch (MalformedURLException ex) {
                System.out.println("stupid");
                Logger.getLogger(Photoic.class.getName()).log(Level.SEVERE, null, ex);
            }
             int width=ImageUtilities.getMat(image).width();
             int height=ImageUtilities.getMat(image).height();
             Dimension d=RatioScaler.getScaleDimension(new Dimension(width,height), new Dimension(1000,600));
             image=RatioScaler.scale(image, d.width, d.height);
             Layer newL=addLayer(image,0,"");
             Universal.addUndo(new addLayerUndo(place,this,newL));
        }
    });
    saveFile.setOnAction((ActionEvent event) -> {
        SaveProject.launchSave(new ProjectObject(this,new Universal()));
    });
    saveasFile.setOnAction((ActionEvent event) -> { 
        SaveProject.launchSaveAs(this);
    });
    openFile.setOnAction((ActionEvent event) -> { 
        OpenProject.launch(this);
    });
    imageMenu=ImageMenu.getMenu(imageMenu, this);
    fileMenu.getItems().addAll(newFile,insert,openFile,saveFile,saveasFile,quitFile);
    menuBar.getMenus().addAll(fileMenu,imageMenu,helpMenu);
    return menuBar;    
        
        
        
    }
    
    private HBox getToolBar() {
        HBox tools=new HBox();
        tools.setSpacing(10);
        ToggleButton eraser=new ToggleButton();ImageView era=new ImageView("Images/eraser.png");era.setFitHeight(22);era.setFitWidth(22);
        eraser.setGraphic(era);eraser.setId("Eraser");eraser.setTooltip(new Tooltip("Eraser")); 
        ToggleButton brush=new ToggleButton();ImageView bru=new ImageView("Images/brush.png");bru.setFitHeight(22);bru.setFitWidth(22);
        brush.setGraphic(bru);brush.setId("Brush");brush.setTooltip(new Tooltip("Brush"));
        ToggleButton marker=new ToggleButton();ImageView mark=new ImageView("Images/marker.png");mark.setFitHeight(22);mark.setFitWidth(22);
        marker.setGraphic(mark);marker.setId("Marker");marker.setTooltip(new Tooltip("Marker"));
        ToggleButton ColorPick=new ToggleButton();ImageView buc=new ImageView("Images/ColorPic.png");buc.setFitHeight(22);buc.setFitWidth(22);
        ColorPick.setGraphic(buc);ColorPick.setId("Color Picker");ColorPick.setTooltip(new Tooltip("Color Picker"));
        ToggleButton move=new ToggleButton();ImageView mov=new ImageView("Images/move.png");mov.setFitHeight(22);mov.setFitWidth(22);
        move.setGraphic(mov);move.setId("Move Arrow");move.setTooltip(new Tooltip("Move Arrow"));
        ToggleButton text=new ToggleButton();ImageView tex=new ImageView("Images/text.png");tex.setFitHeight(22);tex.setFitWidth(22);
        text.setId("Text");text.setTooltip(new Tooltip("Text"));text.setGraphic(tex);
        toolGroup=new ToggleGroup();
        ShapingTools.getToggleButtons();
        toolGroup.getToggles().addAll(brush,marker,move,eraser,text,ColorPick);
        toolGroup.getToggles().addAll(ShapingTools.rectangle,ShapingTools.elipse);
        toolGroup.selectToggle(move);
        place.state=new StateMove(place);
        toolGroup.selectedToggleProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if(newValue==null)newValue=move;
                String selectedToggle=((ToggleButton)newValue).getId();
                if(selectedToggle=="Brush"){place.SwitchState(MouseState.BRUSH);}
                if(selectedToggle=="Marker"){place.SwitchState(MouseState.PENTOOL);}
                if(selectedToggle=="Color Picker"){place.SwitchState(MouseState.PAINTFILL);}
                if(selectedToggle=="Move Arrow"){place.SwitchState(MouseState.MOVE);}
                if(selectedToggle=="Eraser"){place.SwitchState(MouseState.ERASER);}
                if(selectedToggle=="Rectangle"){place.SwitchState(MouseState.S_RECT);}
                if(selectedToggle=="Elipse"){place.SwitchState(MouseState.S_ELI);}
                if(selectedToggle=="Polygon"){place.SwitchState(MouseState.S_POLY);}
                if(selectedToggle=="Text"){place.SwitchState(MouseState.TEXT);}
                
            }
        });
        saveLossless=new Button();
        ImageView temp=new ImageView("Images/save.png");temp.setFitHeight(10);temp.setFitWidth(10);
        saveLossless.setGraphic(temp);
        saveLossless.setStyle("-fx-border-style: solid inside;" +"-fx-border-width: 2;" +"-fx-border-insets: 5;" +"-fx-border-radius: 5;" +
                              "-fx-border-color: green;");
        saveLossless.setOnAction((ActionEvent event) -> {
                if(toolGroup.getSelectedToggle().equals(move))place.SwitchState(MouseState.MOVE);;
                saveLossless.setVisible(false);
        });
        
        tools.getChildren().addAll(move,brush,marker,eraser,text,ColorPick,ShapingTools.getShapingTools(),saveLossless,StrokeSettings.getLayout(),fontChooser);
        
        return tools;
    }
    private HBox getActionButtons(){
        HBox h=new HBox();
        
        Button newLayer=new Button("NEW LAYER");
        newLayer.setOnAction((ActionEvent event) -> {
        NewLayer.launch(this);
        });
        
        
        
    
    h.getChildren().addAll(newLayer);
    h.setSpacing(10);
    return h;
    }
    
    private void keyListenerMove(KeyEvent event){
        KeyCombination combUp= new KeyCodeCombination(KeyCode.UP,KeyCodeCombination.CONTROL_ANY);
        KeyCombination combDown= new KeyCodeCombination(KeyCode.DOWN,KeyCodeCombination.CONTROL_ANY);
        KeyCombination combRight= new KeyCodeCombination(KeyCode.RIGHT,KeyCodeCombination.CONTROL_ANY);
        KeyCombination combLeft= new KeyCodeCombination(KeyCode.LEFT,KeyCodeCombination.CONTROL_ANY);
        KeyCombination shiftLeft= new KeyCodeCombination(KeyCode.LEFT,KeyCodeCombination.SHIFT_ANY);
        KeyCombination shiftRight= new KeyCodeCombination(KeyCode.RIGHT,KeyCodeCombination.SHIFT_ANY);
        KeyCombination shiftA= new KeyCodeCombination(KeyCode.A,KeyCodeCombination.SHIFT_ANY);
        KeyCombination shiftB= new KeyCodeCombination(KeyCode.B,KeyCodeCombination.SHIFT_ANY);
        KeyCombination contT= new KeyCodeCombination(KeyCode.T,KeyCodeCombination.CONTROL_ANY);
        KeyCombination contE= new KeyCodeCombination(KeyCode.E,KeyCodeCombination.CONTROL_ANY);
        KeyCombination contZ= new KeyCodeCombination(KeyCode.Z,KeyCodeCombination.CONTROL_ANY);
        KeyCombination contU= new KeyCodeCombination(KeyCode.U,KeyCodeCombination.CONTROL_ANY);
        KeyCombination contD= new KeyCodeCombination(KeyCode.D,KeyCodeCombination.CONTROL_ANY);
        KeyCombination contC= new KeyCodeCombination(KeyCode.C,KeyCodeCombination.CONTROL_ANY);
    if(combUp.match(event)){
    place.moveUp(selected,1);
    Universal.addUndo(new MoveUndo(0,1,selected,place));
    }
    if(combDown.match(event)){
    place.moveDown(selected,1);
    Universal.addUndo(new MoveUndo(0,-1,selected,place));
    }
    if(combRight.match(event)){
    place.moveRight(selected,1);
    Universal.addUndo(new MoveUndo(1,0,selected,place));
    }
    if(combLeft.match(event)){
    place.moveLeft(selected,1);
    Universal.addUndo(new MoveUndo(-1,0,selected,place));
    }
    if(shiftLeft.match(event)){
    Universal.addUndo(new ResizeUndo(-1,0,selected.losslessMat,selected,place));
    place.shrinkWidth(selected,1);
    }
    if(shiftA.match(event)){
    Universal.addUndo(new ResizeUndo(0,-1,selected.losslessMat,selected,place));
    place.shrinkHeight(selected,1);
    }
    if(shiftB.match(event)){
    Universal.addUndo(new ResizeUndo(0,1,selected.losslessMat,selected,place));
    place.expandHeight(selected,1);
    }
    if(shiftRight.match(event)){
    Universal.addUndo(new ResizeUndo(1,0,selected.losslessMat,selected,place));
    place.expandWidth(selected,1);
    }
    if(contT.match(event)){
    place.shrinkRatio(selected);
    }
   if(contE.match(event)){
    place.expandRatio(selected);
    }
    if(contZ.match(event)){
    Universal.undo();
    }
    if(contC.match(event)){
    Layer newL=addLayer(layerFactory.copy(selected),layerList.indexOf(getLayerContainer(selected.getName()))+1);
    Universal.addUndo(new addLayerUndo(place,this,newL));
    }
    if(contU.match(event)){
    int old=place.layers.indexOf(selected);   
    int ne=place.layers.indexOf(selected)-1;
    if(this.moveLayer(selected,old,ne)){
    Universal.addUndo(new LayerShiftUndo(place,this,selected,old,ne));
    }
    }
    if(contD.match(event)){
    int old=place.layers.indexOf(selected);   
    int ne=place.layers.indexOf(selected)+1;
    if(this.moveLayer(selected,old,ne)){
    Universal.addUndo(new LayerShiftUndo(place,this,selected,old,ne));
    }
    }
    if(event.getCode()==KeyCode.DELETE){
    place.state.deletePressed();
    }
    
    
    }
    
    public Layer addLayer(Image img,int index,String name){
        Layer layer;
        if(img==null){layer=layerFactory.getImageLayer(name,1);}
        else{layer=layerFactory.getImageLayer(img,name,1);}
        layerList.add(index,layer.getPreview());
        layers.getItems().add(index,layer.getPreview().getLayout());
        place.addLayer(layer,index);
        this.selected=layer;
        place.selected=this.selected;
        layers.getSelectionModel().clearSelection();
        layers.getSelectionModel().select(layer.getPreview().getLayout());  
        layers.refresh();
        return layer;
    }
    public Layer addLayer(Layer l,int index){
        Layer layer=l;
        layerList.add(index,layer.getPreview());
        layers.getItems().add(index,layer.getPreview().getLayout());
        place.addLayer(layer,index);
        this.selected=layer;
        place.selected=this.selected;
        layers.getSelectionModel().clearSelection();
        layers.getSelectionModel().select(layer.getPreview().getLayout()); 
        place.update();
        layers.refresh();
        return layer;
    }
    
    public void deleteLayer(Layer l){
           layerList.remove(l.getPreview());
           String toBedeleted=getLayerContainer(l.getName());
           place.deleteLayer(l);
           layers.getItems().remove(toBedeleted);
           layers.refresh();
           System.out.print("deleted ");
           System.out.println(l.name);
           place.update();
    }
    
    public boolean moveLayer(Layer l,int oldpos,int pos){
        System.out.println("to be swapped "+oldpos+" "+pos);
    if(pos<0){return false;}
    if(layerList.size()<=pos){return false;}
    Collections.swap(layerList, oldpos, pos);
    Collections.swap(layers.getItems(), oldpos, pos);
    Collections.swap(place.layers,oldpos, pos);
    System.out.println("swapped "+oldpos+" "+pos);
    layers.getSelectionModel().clearSelection();
    layers.getSelectionModel().select(l.getPreview().getLayout());
    layers.refresh();
    place.update();
    return true;
    }
    public void deleteAll(){
    layerList.clear();
    while(layers.getItems().size()>0){layers.getItems().remove(0);}
    place.layers.clear();
    layers.refresh();
    place.update();
    }

    
    public boolean isLayerExists(String name){
     for(int i=0;i<layerList.size();i++){
    if(name.equals(layerList.get(i).getLayerName())){
    return true;
    }
    }
    return false;
    }

    public void mergeAll(ArrayList<Layer> toMerge) {
        System.out.println("to be merged "+toMerge);
        Image m=place.merge(toMerge);
        Layer master=toMerge.get(toMerge.size()-1);
        MergeUndo undo=new MergeUndo(place,master);
        for(int i=0;i<toMerge.size();i++){
        if(!master.equals(toMerge.get(i))){
        undo.addDeleteUndo(new deleteLayerUndo(place,this,toMerge.get(i),layerList.indexOf(toMerge.get(i).preview)));
        deleteLayer(toMerge.get(i));}
        }
        Universal.addUndo(undo);
        master.setMat(ImageUtilities.getMat(m), master.rectangle);
        layers.refresh();
        layers.getSelectionModel().clearSelection();
        layers.getSelectionModel().select(master.getPreview().getLayout());
        place.update(); 
    }

        
        
    
}


