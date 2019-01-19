const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
var db = admin.firestore();

exports.updateUser = functions.firestore
    .document('users/{userId}/chatdata/{messageId}')
    .onWrite((change, context) => {

const newValue = change.after.data();
console.log(newValue)

var sender = context.params.userId
var chattingToUserId = context.params.messageId
var otherdata = context.params.messageCollectionId
 console.log('sender ' + sender);
 console.log('chatting to id ' +chattingToUserId  );


   return db.collection('users').doc(chattingToUserId)
      .get()
      .then(doc => {
        if (!doc.exists) {
          console.log('No such User document!');
          throw new Error('No such User document!'); //should not occur normally as the notification is a "child" of the user
        } else {
          console.log('Document data:', doc.data());
          console.log('Document data:', doc.data().token);

        var tokenId = doc.data().token


          db.collection('users').doc(sender)
          .get()
	      .then(doc =>{
          if (!doc.exists) {
              console.log('No such User document!');
              throw new Error('No such User document!'); //should not occur normally as the notification is a "child" of the user
            } else {
           var senderName = doc.data().username
           var photoUrl = doc.data().picture
            var userId = doc.data().uniqueID
	var message = {
  	data: {
    	msg: senderName,
    	picture_url: photoUrl,
	    uId: userId,
	},
  		token: tokenId
	};
	admin.messaging().send(message)
  	.then((response) => {
	    	// Response is a message ID string.
    	console.log('Successfully sent message:', response);
  	}).catch((error) => {
    	console.log('Error sending message:', error);
      })
          return true;
          };

            })
        }

});
})
