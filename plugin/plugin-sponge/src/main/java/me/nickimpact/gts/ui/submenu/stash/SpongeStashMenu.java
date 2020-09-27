package me.nickimpact.gts.ui.submenu.stash;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.impactor.api.gui.InventoryDimensions;
import com.nickimpact.impactor.api.utilities.mappings.Tuple;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import me.nickimpact.gts.api.listings.Listing;
import me.nickimpact.gts.api.listings.auctions.Auction;
import me.nickimpact.gts.api.listings.buyitnow.BuyItNow;
import me.nickimpact.gts.api.stashes.Stash;
import me.nickimpact.gts.common.config.MsgConfigKeys;
import me.nickimpact.gts.common.config.wrappers.TitleLorePair;
import me.nickimpact.gts.common.messaging.messages.listings.buyitnow.removal.BuyItNowRemoveRequestMessage;
import me.nickimpact.gts.common.plugin.GTSPlugin;
import me.nickimpact.gts.common.ui.Historical;
import me.nickimpact.gts.sponge.listings.SpongeListing;
import me.nickimpact.gts.sponge.pricing.provided.MonetaryPrice;
import me.nickimpact.gts.sponge.ui.SpongeAsyncPage;
import me.nickimpact.gts.sponge.utils.Utilities;
import me.nickimpact.gts.ui.SpongeMainMenu;
import me.nickimpact.gts.ui.submenu.browser.SpongeSelectedListingMenu;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.nickimpact.gts.sponge.utils.Utilities.PARSER;
import static me.nickimpact.gts.sponge.utils.Utilities.readMessageConfigOption;

public class SpongeStashMenu extends SpongeAsyncPage<Tuple<Listing, Boolean>> implements Historical<SpongeMainMenu> {

    public SpongeStashMenu(Player viewer) {
        super(GTSPlugin.getInstance(),
                viewer,
                GTSPlugin.getInstance().getStorage().getStash(viewer.getUniqueId()).thenApply(Stash::getStashContents)
        );
        this.applier(content -> {
            SpongeListing listing = (SpongeListing) content.getFirst();
            SpongeIcon icon = new SpongeIcon(listing.getEntry().getDisplay(viewer.getUniqueId(), listing).get());
            icon.addListener(clickable -> {
                //new SpongeSelectedListingMenu(this.getViewer(), listing).open();
            });
            return icon;
        });
    }

    @Override
    protected Text getTitle() {
        return PARSER.parse(GTSPlugin.getInstance().getMsgConfig().get(MsgConfigKeys.UI_MENU_STASH_TITLE), Lists.newArrayList(this::getViewer));
    }

    @Override
    protected Map<PageIconType, PageIcon<ItemType>> getPageIcons() {
        Map<PageIconType, PageIcon<ItemType>> options = Maps.newHashMap();
        options.put(PageIconType.PREV, new PageIcon<>(ItemTypes.ARROW, 37));
        options.put(PageIconType.NEXT, new PageIcon<>(ItemTypes.ARROW, 43));

        return options;
    }

    @Override
    protected InventoryDimensions getContentZone() {
        return new InventoryDimensions(7, 2);
    }

    @Override
    protected Tuple<Integer, Integer> getOffsets() {
        return new Tuple<>(1, 1);
    }

    @Override
    protected Tuple<Long, TimeUnit> getTimeout() {
        return new Tuple<>(3L, TimeUnit.SECONDS);
    }

    @Override
    protected SpongeLayout design() {
        SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
        slb.rows(SpongeIcon.BORDER, 0, 3).columns(SpongeIcon.BORDER, 0, 8).slots(SpongeIcon.BORDER, 38, 42);

        SpongeIcon collect = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.HOPPER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_TITLE)))
                .add(Keys.ITEM_LORE, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_ICON_STASH_COLLECT_ALL_LORE)))
                .build()
        );
        collect.addListener(clickable -> {
            AtomicInteger successful = new AtomicInteger();
            for(Tuple<Listing, Boolean> entry : ImmutableList.copyOf(this.getContents())) {
                if(entry.getSecond()) {
                    Listing listing = entry.getFirst();
                    if(listing instanceof Auction) {
                        if(new MonetaryPrice(((Auction) listing).getHighBid().getSecond()).reward(this.getViewer().getUniqueId())) {
                            GTSPlugin.getInstance().getMessagingService().requestAuctionClaim(
                                    listing.getID(),
                                    listing.getLister(),
                                    this.getViewer().getUniqueId().equals(listing.getLister()),
                                    response -> {}
                            );
                            successful.incrementAndGet();
                        }
                    } else {
                        if(((BuyItNow) listing).getPrice().reward(this.getViewer().getUniqueId())) {
                            GTSPlugin.getInstance().getMessagingService().requestBINRemoveRequest(listing.getID(), listing.getLister(), response -> {});
                            successful.incrementAndGet();
                        }
                    }
                } else {
                    if(entry.getFirst().getEntry().give(this.getViewer().getUniqueId())) {
                        GTSPlugin.getInstance().getMessagingService().requestBINRemoveRequest(entry.getFirst().getID(), entry.getFirst().getLister(), response -> { });
                        successful.incrementAndGet();
                    }
                }
            }

            this.getViewer().sendMessage(PARSER.parse(Utilities.readMessageConfigOption(MsgConfigKeys.STASH_COLLECT_ALL_RESULTS), Lists.newArrayList(successful::get)));
            this.getView().close(this.getViewer());
        });
        slb.slot(collect, 41);

        SpongeIcon back = new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.BARRIER)
                .add(Keys.DISPLAY_NAME, PARSER.parse(readMessageConfigOption(MsgConfigKeys.UI_GENERAL_BACK), Lists.newArrayList(this::getViewer)))
                .build()
        );
        back.addListener(clickable -> {
            this.cancelIfRunning();
            this.getParent().ifPresent(parent -> parent.get().open());
        });
        slb.slot(back, 39);

        return slb.build();
    }

    @Override
    protected SpongeUI build(SpongeLayout layout) {
        return SpongeUI.builder()
                .title(this.title)
                .dimension(InventoryDimension.of(9, 5))
                .build()
                .define(this.layout);
    }

    @Override
    protected SpongeIcon getLoadingIcon() {
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, PARSER.parse(
                        Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_LOADING),
                        Lists.newArrayList(this::getViewer)
                ))
                .add(Keys.DYE_COLOR, DyeColors.YELLOW)
                .build()
        );
    }

    @Override
    protected SpongeIcon getTimeoutIcon() {
        TitleLorePair pair = Utilities.readMessageConfigOption(MsgConfigKeys.UI_MENU_LISTINGS_SPECIAL_TIMED_OUT);
        return new SpongeIcon(ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DISPLAY_NAME, PARSER.parse(pair.getTitle(), Lists.newArrayList(this::getViewer)))
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.ITEM_LORE, PARSER.parse(pair.getLore(), Lists.newArrayList(this::getViewer)))
                .build()
        );
    }

    @Override
    protected Consumer<List<Tuple<Listing, Boolean>>> applyWhenReady() {
        return stash -> {};
    }

    @Override
    public Optional<Supplier<SpongeMainMenu>> getParent() {
        return Optional.of(() -> new SpongeMainMenu(this.getViewer()));
    }
}
