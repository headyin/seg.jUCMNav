package seg.jUCMNav.strategies;

import grl.Contribution;
import grl.Decomposition;
import grl.Dependency;
import grl.ElementLink;
import grl.Evaluation;
import grl.EvaluationStrategy;
import grl.Feature;
import grl.GRLLinkableElement;
import grl.IntentionalElement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import seg.jUCMNav.Messages;
import seg.jUCMNav.extensionpoints.IGRLStrategyAlgorithm;
import seg.jUCMNav.featureModel.util.DetermineSelectableFeatureCommand;
import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.util.MetadataHelper;
import seg.jUCMNav.model.util.StrategyEvaluationRangeHelper;
import seg.jUCMNav.strategies.util.IntentionalElementUtil;
import urn.URNspec;
import urncore.Metadata;

/**
 * This class implement the conditional GRL evaluation algorithm.
 * 
 * @author Yanji Liu, Yukun Su
 * 
 */
public class FeatureModelStrategyAlgorithm extends FormulaBasedGRLStrategyAlgorithm {

    List strategyMetaDataValue;
    HashMap acceptStereotypes = new HashMap<String, String>();

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.extensionpoints.IGRLStrategiesAlgorithm#init(java.util.Vector)
     */
    public void init(EvaluationStrategy strategy, HashMap evaluations) {
        MetadataHelper.cleanRunTimeMetadata(strategy.getGrlspec().getUrnspec());
        List sMetaData = strategy.getMetadata();
        Metadata acceptStereotype;
        // could be multiple ones, so not using MetadataHelper
        for (int i = 0; i < sMetaData.size(); i++) {
            acceptStereotype = (Metadata) sMetaData.get(i);
            if (acceptStereotype.getName().equalsIgnoreCase(Messages.getString("ConditionalGRLStrategyAlgorithm_acceptStereotype"))) { //$NON-NLS-1$                        
                acceptStereotypes.put(acceptStereotype.getValue().toUpperCase(), acceptStereotype.getValue());
            }
        }
        
        super.init(strategy, evaluations);
    }
    /**
     * Define the strategy to calculate the evaluation. Note that EvaluationStrategy are associated only to Evaluation defined by the user. To access the list
     * of IntentionalElement, use GRLspec (get from the strategy)
     * 
     * This method will init with the root elements only for feature model graph, instead of leaf elements like the init() method.
     * This method in that class will behave the same as init().
     * 
     * @param strategy
     *            EvaluationStrategy used for the calculation
     * @param evaluations
     *            HashMap containing the pair of IntentionalElement->Evaluation defined in this strategy.
     */
    public void initTopDownFeature(EvaluationStrategy strategy, HashMap evaluations) {
    	evalReady = new Vector();
    	evalReadyUserSet = new HashMap();
    	evaluationCalculation = new HashMap();
    	this.evaluations = evaluations;
    	// determines whether -100 or 0 should be used as a minimum scale.
    	minRange = -100 * (StrategyEvaluationRangeHelper.getCurrentRange(strategy.getGrlspec().getUrnspec()) ? 0 : 1);
		ListIterator it = strategy.getGrlspec().getIntElements().listIterator(strategy.getGrlspec().getIntElements().size());
		while (it.hasPrevious()) {
			IntentionalElement element = (IntentionalElement) it.previous();
			int srcLinkNum = 0;
			Iterator linkIt = element.getLinksSrc().iterator();
			while (linkIt.hasNext())
			{
				ElementLink link = (ElementLink) linkIt.next();
				if (link.getDest() instanceof Feature) {
					srcLinkNum++;
				}			
			}
			if (srcLinkNum == 0) {
					evalReady.add(element);
			} else if (((Evaluation) evaluations.get(element)).getStrategies() != null) {
					EvaluationCalculation calculation = new EvaluationCalculation(element, element.getLinksSrc().size());
					evalReadyUserSet.put(element, calculation);
			} else {
					EvaluationCalculation calculation = new EvaluationCalculation(element, element.getLinksSrc().size());
					evaluationCalculation.put(element, calculation);
			}
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.extensionpoints.IGRLStrategiesAlgorithm#getEvaluationType()
     */
    public int getEvaluationType() {
        return IGRLStrategyAlgorithm.EVAL_FEATURE_MODEL;
    }

    /**
     * This method decides whether an element needs to be ignored or not if an element has ConditionalGRLStrategyAlgorithm_IgnoreNode defined as metadata, it
     * should be ignored regardless if an element does not have any metadata it should never be ignored if an element has stereotype metadata then if the
     * metadata matches the strategy accept stereotype list it should NOT be ignored if the metadata does not match the strategy accept stereotype list it
     * should be ignored
     * 
     * @param element
     * @return
     */
    public boolean checkIgnoreElement(GRLLinkableElement element) {
        List eMetaData = element.getMetadata();
        Metadata elementMetadata;
        boolean foundAcceptacceptStereotype = false;
        int foundStereotype = 0;
        if (MetadataHelper.getMetaData(element, Messages.getString("ConditionalGRLStrategyAlgorithm_IgnoreNode")) != null) //$NON-NLS-1$
        {
            return true;
        } else {
            for (int i = 0; i < eMetaData.size(); i++) {
                elementMetadata = (Metadata) eMetaData.get(i);
                if (acceptStereotypes.size() > 0 && !foundAcceptacceptStereotype) {
                    String val = elementMetadata.getValue();
                    if (val != null)
                        val.toUpperCase();
                    boolean isAcceptedStereotype = acceptStereotypes.containsKey(val);
                    if (elementMetadata.getName().toUpperCase().startsWith("ST_")) {
                        foundStereotype++;
                        if (isAcceptedStereotype)
                            foundAcceptacceptStereotype = true;
                    }
                }
            }
        }

        // if there is not stereotype the element won't be ignored
        foundAcceptacceptStereotype = (foundAcceptacceptStereotype || foundStereotype == 0);
        return !foundAcceptacceptStereotype;
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.extensionpoints.IGRLStrategiesAlgorithm#getEvaluation(grl.IntentionalElement)
     */
    public int getEvaluation(IntentionalElement element) {
        int result = 0;
        Evaluation eval = (Evaluation) evaluations.get(element);
        if (element.getLinksDest().size() == 0) {
            result = eval.getEvaluation();
        } else {
        	int decompositionValue = -10000;
        	int dependencyValue = 10000;
            int[] contributionValues = new int[100];
            /* evaluation values of the nodes connected to this node via contribution links */
            int[] evaluationValues = new int[100];
            /* contribution value of the contribution links connected to the node */
            int[] contributionLinksValues = new int[100];
            /* used to keep a reference to the contribution links to be able to add metadata later on if required */
            ElementLink[] contributionLinks = new ElementLink[100];
            /* used to keep the contribution values that have to be ignored due to unsatisfied dependency */
            int[] ignoredContributionValue = new int[100];
            int contribArrayIt = 0;
            int ignoredContribArrayIt = 0;
            int consideredContribArrayIt = 0;
            int sumConsideredContributionLinks = 0;
            /* The following 3 variables will be used when the diagram is a feature model*/
            int mandatoryLinksIndex = 0;
            boolean onlyOptionalLinks = true;

            if(element instanceof Feature)
            {
                mandatoryLinksIndex = IntentionalElementUtil.getNumberOfMandatoryDestLinks(element);
            	onlyOptionalLinks = IntentionalElementUtil.containsOnlyOptionalDestLink(element);
            }

            Iterator it = element.getLinksDest().iterator(); // Return the list of elementlink
            int remainingContributionFMD = 100; // remaining contribution is currently 100
            while (it.hasNext()) {
                ElementLink link = (ElementLink) it.next();
                if (link instanceof Decomposition) {
                    decompositionValue = evaluateDecomposition(element, decompositionValue, it, link);
                } else if (link instanceof Dependency) {
                    dependencyValue = evaluateDependency(dependencyValue, link);

                    IntentionalElement src = (IntentionalElement) link.getSrc();
                    if (src.getType().getName().equals("Ressource")) { //$NON-NLS-1$
                        boolean ignoreSrc = false;
                        ignoreSrc = checkIgnoreElement(src);
                        URNspec urnSpec = element.getGrlspec().getUrnspec();
                        if (dependencyValue == 0 && !ignoreSrc) {
                            MetadataHelper.addMetaData(urnSpec, element, Messages.getString("ConditionalGRLStrategyAlgorithm_IgnoreNode"), ""); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
                        }
                        if (ignoreSrc) {
                            MetadataHelper.addMetaData(urnSpec, src, Messages.getString("ConditionalGRLStrategyAlgorithm_IgnoreNode"), ""); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
                            dependencyValue = 10000;
                        }
                    }
                } else if (link instanceof Contribution) {
                    Contribution contrib = (Contribution) link;

                    boolean ignoreSrc = false;
                    ignoreSrc = checkIgnoreElement(link.getSrc());

                    int quantitativeContrib = EvaluationStrategyManager.getInstance().getActiveQuantitativeContribution(contrib);
                    // if Feature Model Diagram
                    if(element instanceof Feature)
                    {
                    	if(onlyOptionalLinks)
                    		//The case that the element contains only optional links
                    		quantitativeContrib = 100;
                    	else
                    	{
                    		//Mixed case or only mandatory links
                    		if(ModelCreationFactory.containsMetadata(link.getMetadata(), ModelCreationFactory.getFeatureModelMandatoryLinkMetadata()))
                    		{
                    			//Last Link case
                    			if(mandatoryLinksIndex == 1)                			
                    				quantitativeContrib = remainingContributionFMD;
                    			else {
                    				quantitativeContrib = remainingContributionFMD/mandatoryLinksIndex;
                    				remainingContributionFMD -= quantitativeContrib;
                    			}
                    			mandatoryLinksIndex--;
                    		}else
                    			//The link is an optional link
                    			quantitativeContrib = 0;                		
                    	}
                    	contrib.setQuantitativeContribution(quantitativeContrib);

                    }

                    if (ignoreSrc) {
                        ignoredContributionValue[ignoredContribArrayIt] = quantitativeContrib;
                        ignoredContribArrayIt++;
                        URNspec urnSpec = element.getGrlspec().getUrnspec();
                        MetadataHelper.addMetaData(urnSpec, link.getSrc(), Messages.getString("ConditionalGRLStrategyAlgorithm_IgnoreNode"), ""); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
                    } else {
                        contributionLinksValues[consideredContribArrayIt] = quantitativeContrib;
                        contributionLinks[consideredContribArrayIt] = link;
                        int srcNodeEvaluationValue = ((Evaluation) evaluations.get(link.getSrc())).getEvaluation();
                        evaluationValues[consideredContribArrayIt] = srcNodeEvaluationValue;

                        sumConsideredContributionLinks = sumConsideredContributionLinks + contributionLinksValues[consideredContribArrayIt];

                        consideredContribArrayIt++;

                        double resultContrib = super.computeContributionResult(link, contrib);

                        if (resultContrib != 0) {
                            contributionValues[contribArrayIt] = (new Double(Math.round(resultContrib))).intValue();
                            contribArrayIt++;
                        }
                    }
                }
            }

            if (ignoredContribArrayIt > 0 && consideredContribArrayIt > 0 && sumConsideredContributionLinks > 0) {
                int totalIgnoredContributionValue = 0;
                for (int i = 0; i < ignoredContribArrayIt; i++) {
                    totalIgnoredContributionValue = totalIgnoredContributionValue + ignoredContributionValue[i];
                }

                int additionalContributionToRemainingNodes = totalIgnoredContributionValue;

                contributionValues = new int[100];
                contribArrayIt = 0;
                for (int j = 0; j < consideredContribArrayIt; j++) {

                    contributionLinksValues[j] = contributionLinksValues[j]
                            + (additionalContributionToRemainingNodes * contributionLinksValues[j] / sumConsideredContributionLinks);

                    if (contributionLinksValues[j] > 100) {
                        contributionLinksValues[j] = 100;
                    } else if (contributionLinksValues[j] < -100) {
                        contributionLinksValues[j] = -100;
                    }

                    URNspec urnSpec = element.getGrlspec().getUrnspec();
                    MetadataHelper.addMetaData(urnSpec, contributionLinks[j], Messages.getString("ConditionalGRLStrategyAlgorithm_RuntimeContribution"), //$NON-NLS-1$
                            Integer.toString(contributionLinksValues[j]));

                    double resultContrib;

                    resultContrib = (contributionLinksValues[j] * evaluationValues[j]) / 100;

                    if (resultContrib != 0) {

                        contributionValues[contribArrayIt] = (new Double(Math.round(resultContrib))).intValue();

                        contribArrayIt++;
                    }
                }

            }
            result = ensureEvaluationWithinRange(result, decompositionValue, dependencyValue, contributionValues, contribArrayIt);
        }
        if (eval.getIntElement() != null) {
        	if (result != eval.getEvaluation()) {
        		Metadata warning = MetadataHelper.getMetaDataObj(element, "user_set_evaluation_warning");
        		if (warning == null) {
        			warning = ModelCreationFactory.getUserSetEvalWarningMetadata();
        			element.getMetadata().add(warning);
        		}
    			warning.setValue(Integer.toString(eval.getEvaluation()) + " != " + Integer.toString(result));
        	} else {
        		//user set evaluation is equal to evaluated value
        		Metadata warning = MetadataHelper.getMetaDataObj(element, "user_set_evaluation_warning");
        		if (warning != null) {
        			element.getMetadata().remove(warning);
        		}
        	}
        	result = eval.getEvaluation();
        } else {
    		Metadata warning = MetadataHelper.getMetaDataObj(element, "user_set_evaluation_warning");
    		if (warning != null) {
    			element.getMetadata().remove(warning);
    		}
        }
        return result;
    }

}
