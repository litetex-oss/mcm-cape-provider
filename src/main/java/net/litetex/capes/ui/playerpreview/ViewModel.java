package net.litetex.capes.ui.playerpreview;

import net.litetex.capes.ui.playerpreview.render.PlayerDisplayGuiPayload;


public interface ViewModel
{
	void toggleShowBody();
	
	void toggleShowElytra();
	
	PlayerDisplayGuiPayload getPayload();
}
