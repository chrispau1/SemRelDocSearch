package de.paul.kb;

import java.util.Set;

import de.paul.annotations.Annotatable;
import de.paul.annotations.Category;

public interface HierarchicalKnowledgeBase {

	/**
	 * Get the depth of a category in the hierarchical knowledge base
	 * 
	 * @param categoryName
	 * @return
	 */
	public int getDepth(String categoryName);

	/**
	 * Get ancestors for an annotation, using its categories to link it to the
	 * hierarchical knowledge base.
	 * 
	 * @param ann
	 *            The annotation whose ancestors are sought.
	 * @param categories
	 *            Categories of the annotation, used to find ancestors.
	 * @return
	 */
	public Set<Category> getAncestors(Annotatable ann, Set<String> categories);

}
