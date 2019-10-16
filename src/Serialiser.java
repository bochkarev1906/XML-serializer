import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class Serialiser {
    public void serialize(Object obj)  {
        try {
            Document document = DocumentHelper.createDocument();
            Class<?> clazz = obj.getClass(); //получить класс объекта во время выполнения
            if (clazz.isAnnotationPresent(XmlObject.class)) { //проверка аннотации и получение доступа к ее полям?
                XmlObject xmlObject = clazz.getAnnotation(XmlObject.class); //получить аннотацию
                String nameClass;
                if (xmlObject.name().equals("")) {
                    nameClass = clazz.getName();
                }
                else {
                    nameClass = xmlObject.name();
                }
                Element root = document.addElement(nameClass); //корневой элемент (person)
                Field[] fields = clazz.getDeclaredFields();
                Method[] methods = clazz.getDeclaredMethods();
                HashMap<String, ArrayList<Attribute>> tagsAndAttributes = new HashMap<>();

                for (Field field : fields) {
                    if (field.isAnnotationPresent(XmlAttribute.class)) { //проверка аннотации
                        XmlAttribute attribute = field.getAnnotation(XmlAttribute.class);
                        String nameAttribute;
                        if (attribute.name().equals("")) {
                            nameAttribute = field.getName();
                        }
                        else {
                            nameAttribute = attribute.name();
                        }
                        field.setAccessible(true); //разрешаем доступ к полю
                        String value = field.get(obj).toString();
                        addAttributeInMap(tagsAndAttributes, attribute, nameAttribute, value);
                    }
                }

                for (Method method : methods) {
                    if (method.isAnnotationPresent(XmlAttribute.class)) { //проверка аннотации
                        XmlAttribute attribute = method.getAnnotation(XmlAttribute.class);
                        String nameAttribute;
                        if (attribute.name().equals("")) {
                            nameAttribute = method.getName();
                        }
                        else {
                            nameAttribute = attribute.name();
                        }
                        if (nameAttribute.startsWith("get")) {
                            nameAttribute = nameAttribute.trim().substring(3); //отбрасываем get
                        }
                        method.setAccessible(true); // доступ к методу
                        String value = method.invoke(obj).toString();
                        addAttributeInMap(tagsAndAttributes, attribute, nameAttribute, value);
                    }
                }

                for (Field field : fields)
                    if (field.isAnnotationPresent(XmlTag.class)) {
                        XmlTag tag = field.getAnnotation(XmlTag.class);
                        String nameTag;
                        if (tag.name().equals("")) {
                            nameTag = field.getName();
                        }
                        else {
                            nameTag = tag.name();
                        }
                        Element element = root.addElement(nameTag); //корневой элемент (имя тега)
                        field.setAccessible(true);
                        String value = field.get(obj).toString();
                        element.addText(value);
                        addAttributeInElement(tagsAndAttributes, nameTag, element);
                    }

                for (Method method : methods) {
                    if (method.isAnnotationPresent(XmlTag.class)) {
                        XmlTag tag = method.getAnnotation(XmlTag.class);
                        String nameTag;
                        if (tag.name().equals("")) {
                            nameTag = method.getName();
                        }
                        else {
                            nameTag = tag.name();
                        }
                        if (nameTag.startsWith("get")) {
                            nameTag = nameTag.trim().substring(3);
                        }
                        Element element = root.addElement(nameTag);
                        method.setAccessible(true);
                        String value = method.invoke(obj).toString();
                        element.addText(value);
                        addAttributeInElement(tagsAndAttributes, nameTag, element);
                    }
                }

                if (tagsAndAttributes.get("") != null) {
                    for (Attribute attribute : tagsAndAttributes.get("")) {
                        root.addAttribute(attribute.getName(), attribute.getValue());
                    }
                }
            }
            FileWriter out = new FileWriter("Vladimir.xml");
            document.write(out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ошибка");
        }
    }

    public void addAttributeInMap(HashMap<String, ArrayList<Attribute>> tagAndAttributes, XmlAttribute attribute, String nameAttribute, String value) throws Exception {
        if (tagAndAttributes.containsKey(attribute.tag())) { //существует ли элемент в мапе
            ArrayList<Attribute> newAttributes = tagAndAttributes.get(attribute.tag());
            for (Attribute a : newAttributes) {
                if (a.getName().equals(nameAttribute)){
                    throw new Exception("Два одинаковых атрибута");
                }
            }
            newAttributes.add(new Attribute(nameAttribute, value));
            tagAndAttributes.put(attribute.tag(), newAttributes);
        }
        else {
            ArrayList<Attribute> newAttributes = new ArrayList<>();
            newAttributes.add(new Attribute(nameAttribute, value));
            tagAndAttributes.put(attribute.tag(), newAttributes);
        }
    }

    public void addAttributeInElement(HashMap<String, ArrayList<Attribute>> tagAndAttributes, String nameTag, Element element) {
        if (tagAndAttributes.containsKey(nameTag)) {
            for (Attribute attribute : tagAndAttributes.get(nameTag)) {
                element.addAttribute(attribute.getName(), attribute.getValue());
                tagAndAttributes.remove(nameTag, attribute);
            }
        }
    }

}
