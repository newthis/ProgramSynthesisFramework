import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

class Utils {
    public static Type[] concatArrays(Type[] a1, Type[] a2){
        Type[] resp = new Type[a1.length + a2.length];
        
        for(int i = 0; i < a1.length; i++)
            resp[i] = a1[i];

        for(int i = 0; i < a2.length; i++)
            resp[a1.length + i] = a2[i];

        return resp;
    }
}

interface INode extends Cloneable{
    String name = null;
    Object value = null;
    ArrayList<Type> inpTypes = null;
    Type rType = null;
    ArrayList<INode> inpNodes = null;
    ArrayList<INode> outNodes = null;
    
    Object evaluate(); 

    String getName();
    Object getValue();
    Type[] getInpTypes();
    Type getRType();
    INode[] getInpNodes();
    INode[] getOutNodes();

    void setName(String name);
    void addInpNode(INode node);
    void addOutNode(INode node);

    INode removeInpNode(String name);
    INode removeOutNode(String name);
    
    Object clone();
}

abstract class AbstractNode implements INode{
    protected String name;
    protected Object value;
    protected ArrayList<Type> inpTypes;
    protected Type rType;
    private ArrayList<INode> inpNodes;
    private ArrayList<INode> outNodes;
    protected final Logger LOGGER = Logger.getLogger(this.getClass().toString());

    public AbstractNode(String name, 
                        Type[] inpTypes, 
                        Type rType,
                        INode[] inpNodes){
        this.name = name;
        this.inpTypes = new ArrayList<Type>(Arrays.asList(inpTypes));
        this.inpNodes = new ArrayList<INode>(Arrays.asList(inpNodes));
        this.outNodes = new ArrayList<INode>();
        this.rType = rType;
        
        this.value = null;
        for(INode node : inpNodes)
            node.addOutNode(this);
    }     

    public abstract Object evaluate();
    
    public String getName(){
        return this.name;
    }
    
    public Object getValue(){
        return this.value;
    }
    
    public Type[] getInpTypes(){
        Object[] aux = this.inpTypes.toArray();
        return Arrays.copyOf(aux, aux.length, Type[].class);
    }
    
    public Type getRType(){
        return this.rType;
    }

    public INode[] getInpNodes(){
        Object[] aux = this.inpNodes.toArray();
        return Arrays.copyOf(aux, aux.length, INode[].class);
    }
    
    public INode[] getOutNodes(){
        
        Object[] aux = this.outNodes.toArray();
        return Arrays.copyOf(aux, aux.length, INode[].class);
    }

    public void setName(String name){
        this.name = name;
    }
    
    public void addInpNode(INode node){
        if(!Arrays.asList(this.getInpTypes()).contains(node.getRType()))
            LOGGER.warning(String.format("Invalid Conection! %s => %s", node.getName(), this.getName()));
        this.inpNodes.add(node);
    }
    
    public void addOutNode(INode node){
        if(!Arrays.asList(node.getInpTypes()).contains(this.getRType()))
            LOGGER.warning(String.format("Invalid Conection! %s (%s) => %s(%s)", this.getName(), this.getRType(), node.getName(), Arrays.toString(node.getInpTypes())));
        this.outNodes.add(node);
    }

    public INode removeInpNode(String name){
        INode rmvNode = null;
        
        //TODO: Implement O(1) remove
        for(INode node : this.inpNodes){
            if(node.getName().equals(name))
                rmvNode = node; break;
        }

        inpNodes.remove(rmvNode);
        
        return rmvNode;
    }

    public INode removeOutNode(String name){
        INode rmvNode = null;
        
        //TODO: Implement O(1) remove
        for(INode node : this.outNodes){
            if(node.getName().equals(name))
                rmvNode = node; break;
        }

        outNodes.remove(rmvNode);
        return rmvNode;
    }

    public abstract Object clone();
    
}

class MethodNode extends AbstractNode{
    
    private Method method;
    private Type methodClass;

    public MethodNode(Method method,
                      Type methodClass,
                      String name, 
                      INode[] inpNodes)
    {
        super(name, Utils.concatArrays(new Type[]{methodClass}, method.getGenericParameterTypes()), method.getReturnType(), inpNodes);
        this.method = method;
    }

    protected Object[] getInputs(){
        ArrayList<Object> inputs = new ArrayList<Object>();
        for(INode node : this.getInpNodes()){
            inputs.add(node.evaluate());
        }
        return inputs.toArray();
    }

    public Object evaluate(){
        Object[] inputs = this.getInputs();
        Object output = null;
        try{
            if(inputs.length == 1)
                output = method.invoke(inputs[0]);
            else
                output = method.invoke(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        this.value = output;
        return output;
    }

    public Method getMethod(){
        return this.method;
    }

    public Type getMethodClass(){
        return this.methodClass;
    }

    public Object clone(){
        return new MethodNode(this.getMethod(),
                              this.getMethodClass(),
                              this.getName(),
                              this.getInpNodes());
    }
}

class ConstantNode extends AbstractNode{
        
    public ConstantNode(Object constant, String name){
        super(name, new Type[0], constant.getClass(), new INode[0]);
        this.value = constant;
    }
    
    public Object evaluate(){
        return this.value; 
    }

    public Object clone(){
        return new ConstantNode(this.getValue(), this.getName());
    }
}

public class Main {

  public static void main(String[] args) {
    Method method = null;
    
    try{
        method = String.class.getMethod("substring", int.class, int.class);
    } catch (NoSuchMethodException e){
        e.printStackTrace();
    }
    
    ConstantNode ciNode = new ConstantNode(2, "ci");
    ConstantNode cfNode = new ConstantNode(5, "ce");
    ConstantNode sNode = new ConstantNode("b end lepo", "cs");
    MethodNode mNode = new MethodNode(method, String.class, "m2", new INode[]{sNode, ciNode, cfNode});
    
    System.out.println(mNode.evaluate());
  }
}
