import java.util.NoSuchElementException
import scala.collection.mutable
import akka.actor._
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._
import akka.util.Timeout

case class GetAccountRequest(accountId: String)

case class CreateAccountRequest(initialBalance: Double)

case class IdentifyActor()

class Bank(val bankId: String) extends Actor {

    val accountCounter = new AtomicInteger(1000)

    def createAccount(initialBalance: Double): ActorRef = {
        val i = accountCounter.incrementAndGet()
        val id = s"$i" //Fancy val to string
        val account = BankManager.createAccount(id, this.bankId, initialBalance)
        account
    }

    def findAccount(bankId: String, accountId: String): Option[ActorRef] = {
        try {
            Some(BankManager.findAccount(bankId, accountId))
        } catch {
            case e: NoSuchElementException => None
        }
    }

    def findAccount1(accountId: String): ActorRef = {
        BankManager.findAccount(this.bankId, accountId)
    }

    def findOtherBank(bankId: String): Option[ActorRef] = {
        // Use BankManager to look up a different bank with ID bankId
        try {
            Some(BankManager.findBank(bankId))
        } catch {
            case e: NoSuchElementException => None
        }
    }

    override def receive = {
        case CreateAccountRequest(initialBalance) => 
            sender ! createAccount(initialBalance) // Create a new account
        case GetAccountRequest(id) => 
            sender ! findAccount1(id) // Return account
        case IdentifyActor => sender ! this
        case t: Transaction =>{
            processTransaction(t) // Process incoming Transaction
        }

        case t: TransactionRequestReceipt => {
        // Forward receipt
        val accountNumber = t.toAccountNumber
        val isInternal = t.transaction.from.length <= 4
        val bankId = if (isInternal) this.bankId else t.transaction.from.substring(0, 4)
        findAccount(bankId, t.toAccountNumber) match {
            case Some(a) => a ! t
            case None => println("None, Vi har fått kvittering fra konto som ikke finnes?")
        } 
        }

        case msg => println(s"$msg")
    }

    def processTransaction(t: Transaction): Unit = {
        implicit val timeout = new Timeout(5 seconds)
        val isInternal = t.to.length <= 4
        val toBankId = if (isInternal) bankId else t.to.substring(0, 4)
        val toAccountId = if (isInternal) t.to else t.to.substring(4)
        val transactionStatus = t.status
        
        // This method should forward Transaction t to an account or another bank, depending on the "to"-address.
        // HINT: Make use of the variables that have been defined above.
        
        // Dersom det er internet i banken
        if(isInternal || toBankId == bankId) {
            findAccount(toBankId, toAccountId) match {
                case Some(a) => a ! t
                case None => {
                    println("Account finnes ikke")
                    t.status = TransactionStatus.FAILED

                    sender ! new TransactionRequestReceipt(t.from, t.id, t)
                }
            }
        }

        // Dersom det er en ekstern bank
        else {
            findOtherBank(toBankId) match {
                case Some(b) => b ! t
                case None => {
                    println("Bank finnes ikke")
                    t.status = TransactionStatus.FAILED
                    
                    sender ! new TransactionRequestReceipt(t.from, t.id, t)
                }
            }
        }

    }
}