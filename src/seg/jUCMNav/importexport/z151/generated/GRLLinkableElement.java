//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.9-03/31/2009 04:14 PM(snajper)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.07.19 at 07:21:12 PM EDT 
//


package seg.jUCMNav.importexport.z151.generated;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GRLLinkableElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GRLLinkableElement">
 *   &lt;complexContent>
 *     &lt;extension base="{}GRLmodelElement">
 *       &lt;sequence>
 *         &lt;element name="linksDest" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="linksSrc" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GRLLinkableElement", propOrder = {
    "linksDest",
    "linksSrc"
})
@XmlSeeAlso({
    Actor.class,
    IntentionalElement.class
})
public class GRLLinkableElement
    extends GRLmodelElement
{

    @XmlElementRef(name = "linksDest", type = JAXBElement.class)
    protected List<JAXBElement<Object>> linksDest;
    @XmlElementRef(name = "linksSrc", type = JAXBElement.class)
    protected List<JAXBElement<Object>> linksSrc;

    /**
     * Gets the value of the linksDest property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linksDest property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinksDest().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Object>> getLinksDest() {
        if (linksDest == null) {
            linksDest = new ArrayList<JAXBElement<Object>>();
        }
        return this.linksDest;
    }

    /**
     * Gets the value of the linksSrc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the linksSrc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLinksSrc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Object>> getLinksSrc() {
        if (linksSrc == null) {
            linksSrc = new ArrayList<JAXBElement<Object>>();
        }
        return this.linksSrc;
    }

}
