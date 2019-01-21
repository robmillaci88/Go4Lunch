//package com.example.robmillaci.go4lunch.view_matchers;
//
//class RecyclerViewMatcher {
//
//// --Commented out by Inspection START (21/01/2019 14:55):
////    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
////        checkNotNull(itemMatcher);
////        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
////            @Override
////            public void describeTo(Description description) {
////                description.appendText("has item at position " + position + ": ");
////                itemMatcher.describeTo(description);
////            }
////
////            @Override
////            protected boolean matchesSafely(final RecyclerView view) {
////                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
////                return viewHolder != null && itemMatcher.matches(viewHolder.itemView);
////            }
////        };
////    }
//// --Commented out by Inspection STOP (21/01/2019 14:55)
//}
