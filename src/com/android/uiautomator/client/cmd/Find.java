package com.android.uiautomator.client.cmd;

import com.android.uiautomator.client.*;
import com.android.uiautomator.client.XmlUtils.XmlUtils;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xdf
 *
 */
public class Find extends CommandBase {

	/**
	 *
	 */
	private Elements elements = Elements.getGlobal();

	@Override
	public String execute(JSONObject args) throws JSONException,
			ParserConfigurationException {
		try {
			String strategy = (String) args.get("strategy");
			String selector = (String) args.get("selector");
			Boolean multiple = (Boolean) args.get("multiple");

			strategy = strategy.trim().replace(" ", "_").toUpperCase();
			Object result = null;
			List<UiSelector> selectors = new ArrayList<UiSelector>();
			try {
				selectors = getSelectors(strategy, selector,
						multiple);
			} catch (Exception e) {
				e.printStackTrace();
				return failed(Status.InvalidSelector);
			}
			boolean found = false;

			if (multiple) {
				List<Element> foundElements = new ArrayList<Element>();
				for (UiSelector item : selectors) {
					try {
						final List<Element> elementsFromSelector = getElements(item);
						foundElements.addAll(elementsFromSelector);
					} catch (final UiObjectNotFoundException ignored) {
					}
				}
				found = true;
				result = elementsToJSONArray(foundElements);

			} else {
				for (int i = 0; i < selectors.size() && !found; i++) {
					try {
						result = getElement(selectors.get(i));
						found = result != null;
					} catch (Exception ignored) {
						//Utils.output("ignored selector");
					}
				}
			}

			if (!found) {
				return failed(Status.NoSuchElement);
			}

			return success((Object) result);
		} catch (Exception e) {
			e.printStackTrace();
			return failed(Status.UnknownError);
		}
	}

	/**
	 * @param strategy
	 * @param text
	 * @param multiple
	 * @return res
	 * @throws ParserConfigurationException
	 */
	private List<UiSelector> getSelectors(String strategy, String text,
			boolean multiple) throws Exception {

		final List<UiSelector> list = new ArrayList<UiSelector>();

		UiSelector selectors = new UiSelector();

		if (strategy.equals("CLASS_NAME")) {
			selectors = selectors.className(text);

			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);
		} else if (strategy.equals("NAME")) {
			selectors = new UiSelector().description(text);
			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);

			selectors = new UiSelector().text(text);
			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);
		} else if (strategy.equals("ID")) {
			selectors = selectors.resourceId(text);

			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);
		} else if (strategy.equals("XPATH")) {
			final ArrayList<UiSelector> pairs = XmlUtils.getSelectors(text);

			if (!multiple) {
				if (pairs.size() != 0) {
					list.add(pairs.get(0));
				}
			} else {
				for (final UiSelector pair : pairs) {
					list.add(pair);
				}
			}
		} else if (strategy.equals("LINK_TEXT")) {
			selectors = selectors.text(text);
			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);
		} else if (strategy.equals("PARTIAL_LINK_TEXT")) {
			selectors = selectors.textContains(text);
			if (!multiple) {
				selectors = selectors.instance(0);
			}
			list.add(selectors);
		}

		return list;
	}

	/**
	 * @param sel
	 * @return res
	 * @throws JSONException
	 * @throws Exception
	 */
	private JSONObject getElement(final UiSelector sel) throws JSONException,
			Exception {
		final JSONObject res = new JSONObject();
		final Element element = getElements().getElement(sel);
		return res.put("ELEMENT", element.getId());
	}

	/**
	 * @param sel
	 * @return res
	 * @throws UiObjectNotFoundException
	 */
	private ArrayList<Element> getElements(final UiSelector sel)
			throws UiObjectNotFoundException {
		return elements.getElements(sel);
	}

	/**
	 * @param elems
	 * @return res
	 * @throws JSONException
	 */
	private JSONArray elementsToJSONArray(final List<Element> elems)
			throws JSONException {
		JSONArray resArray = new JSONArray();
		for (Element element : elems) {
			resArray.put(new JSONObject().put("ELEMENT", element.getId()));
		}
		return resArray;
	}

	/**
	 * @return res
	 */
	public Elements getElements() {
		return elements;
	}

	/**
	 * @param elements
	 */
	public void setElements(Elements elements) {
		this.elements = elements;
	}
}
