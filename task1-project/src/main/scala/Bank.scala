import java.util.concurrent.Executors

class Bank(val allowedAttempts: Integer = 3) {

    //private val uid = 0
    private val transactionsQueue: TransactionQueue = new TransactionQueue()
    private val processedTransactions: TransactionQueue = new TransactionQueue()
    private val executorContext = Executors newFixedThreadPool(100)

    private var account_id_counter = 0 // previous unique account ID

    def addTransactionToQueue(from: Account, to: Account, amount: Double): Unit = {
      transactionsQueue push new Transaction(from, to, amount, allowedAttempts)

        executorContext submit new Runnable {
            override def run(): Unit = {
                processTransaction
            }
        }
    }

    def generateAccountId: Int = this.synchronized {
        account_id_counter += 1
        return account_id_counter
    }

    private def processTransaction: Unit = {
        val transaction = transactionsQueue.pop
        transaction run

        if(transaction.status == TransactionStatus.PENDING) {
            transactionsQueue push transaction
            executorContext submit new Runnable {
                override def run(): Unit = {
                    processTransaction
                }
            }

        } else {
            processedTransactions push transaction
        }

    }

    def addAccount(initialBalance: Double): Account = {
        new Account(this, initialBalance)
    }

    def getProcessedTransactionsAsList: List[Transaction] = {
        processedTransactions.iterator.toList
    }

}
