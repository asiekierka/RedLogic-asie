package mods.immibis.redlogic.recipes;

/* NOT USED
public class ColouredRecipeDye extends ColouredRecipe {
	
	public static Map<Character, Object> makeItemMap(Object... data) {
		Map<Character, Object> rv = new HashMap<>();
		for(int index = 0; index < data.length; index += 2) {
			Object o = data[index+1];
			
			if(o instanceof Item)
				o = new ItemStack((Item)o, 1, 32767);
			else if(o instanceof Block)
				o = new ItemStack((Block)o, 1, 32767);
			else if(o instanceof String)
				o = OreDictionary.getOres((String)o);
			else if(o instanceof ItemStack)
				;
			else
				throw new IllegalArgumentException("Cannot use object: "+o);
			
			rv.put((Character)data[index], data[index+1]);
		}
		return rv;
	}
	
	private abstract static class RecipeMatcher {
		abstract boolean matches(ItemStack item);
	}
	
	private static class RecipeMatcherExactMeta extends RecipeMatcher {
		private final Item item;
		private final int meta;
		public RecipeMatcherExactMeta(ItemStack is) {
			item = is.getItem();
			meta = is.getItemDamage();
		}
		
		@Override
		boolean matches(ItemStack item) {
			return item != null && item.getItem() == this.item && item.getItemDamage() == meta; 
		}
	}
	
	private static class RecipeMatcherItem extends RecipeMatcher {
		private final Item item;
		public RecipeMatcherItem(Item item) {
			this.item = item;
		}
		@Override
		boolean matches(ItemStack item) {
			return item != null && item.getItem() == this.item;
		}
	}
	
	private static class RecipeMatcherList extends RecipeMatcher {
		private final List<ItemStack> list;
		public RecipeMatcherList(List<ItemStack> list) {
			this.list = list;
		}
		@Override
		boolean matches(ItemStack item) {
			if(item == null)
				return true;
			for(ItemStack expect : list)
				if(item.getItem() == expect.getItem())
					if(expect.getItemDamage() == OreDictionary.WILDCARD_VALUE || item.getItemDamage() == expect.getItemDamage())
						return true;
			return false;
		}
	}
	
	
		
	
	Object[] matches;
	
	public ColouredRecipeDye(String row1, String row2, String row3, Map<Character, Object> charMap) {
		super(row1 == null ? 1 : row1.length(), row2 == null ? 1 : row3 == null ? 2 : 3);
		
		if(row1 == null || (row2 == null && row3 != null)) throw new IllegalArgumentException();
		if(row1.length() != row2.length() || row2.length() != row3.length()) throw new IllegalArgumentException();
		if(row1.length() < 1 || row1.length() > 3) throw new IllegalArgumentException();
		
	}
	
	@Override
	protected boolean doesMatch(ItemStack input, int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}
}*/
