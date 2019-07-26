import java.util.concurrent.*;
import java.math.BigInteger;


class TumorDiscreto{
	double Ps_,Pd_,Pm_,Pp_,Pq_;
	int Np_;
	public int dimension_,p_,q_;
	int ventana_;
	int[][][] lattice,Ph;

	BigInteger[] seed_;
	private static int nucleos_=1;//Runtime.getRuntime().availableProcessors();

	CyclicBarrier barrera_;
	randomGenerator rand;

	TumorDiscreto(double Ps, double Pd, double Pm, double Pp, double Pq, int Np, int D)
	{
		seed_ = new BigInteger[nucleos_];
		for(int i=0; i<nucleos_ ;++i)
			seed_[i] = new BigInteger(Long.toString(System.currentTimeMillis()));
		dimension_ = D;
		ventana_ = (int)(dimension_/nucleos_);
		barrera_ = new CyclicBarrier(nucleos_);
		rand = new randomGenerator();

		Ps_ = Ps;
		Pd_ = Pd;
		Pm_ = Pm;
		Pp_ = Pp;
		Pq_ = Pq;
		Np_ = Np;

		p_ = 0;
		q_ = 1;
		lattice = new int[D+2][D+2][2];
		Ph = new int[D][D][2];

		for(int i=0; i<=D+1 ; ++i)
		{
			lattice[0][i][0] = 1;
			lattice[0][i][1] = 1;
			lattice[D+1][i][1] = 1;
			lattice[D+1][i][0] = 1;
			lattice[i][0][0] = 1;
			lattice[i][0][1] = 1;
			lattice[i][D+1][0] = 1;
			lattice[i][D+1][1] = 1;
		}

		for(int i=70; i<=190 ; ++i)
			lattice[i][D/2][0] = 1;
	}

	void nextGen()
	{
		ThreadPoolExecutor ejecutor = new ThreadPoolExecutor(nucleos_,nucleos_,0L,TimeUnit.MILLISECONDS
				,new LinkedBlockingQueue<Runnable>());
		int inicio=1,fin=ventana_;


		for(int i = 1; i<=nucleos_ ; ++i)
        {    
        	//System.out.println("inicio: "+inicio+"fin: "+fin);
        	ejecutor.execute(new TumorDiscretoTarea(Ps_,Pd_,Pm_,Pp_,Pq_,Np_,Ph,lattice,seed_,i-1,rand,barrera_,p_,q_,dimension_,fin,inicio));
            inicio += ventana_;
            fin += ventana_;
        }
		try
		{
          ejecutor.shutdown();
          ejecutor.awaitTermination(1L,TimeUnit.DAYS);
        }catch(InterruptedException e){}
       	if(p_ == 0)
	    {
			p_=1;
			q_=0;
	    }else{
			p_=0;
			q_=1;
	    }
	}
}

class TumorDiscretoTarea implements Runnable{
	int dimension,fin,inicio;
	CyclicBarrier barrier_;
	randomGenerator rand_;
	int[][][] lattice_,Ph_;

	int p_,q_,k_;

	double Ps_,Pd_,Pm_,Pp_,Pq_,Np_;
	BigInteger[] seed;
	TumorDiscretoTarea(double Ps, double Pd, double Pm, double Pp, double Pq, int Np,int[][][] ph, int[][][] latt, BigInteger[] s,int k, randomGenerator r,CyclicBarrier barrera, int p, int q, int d, int f, int ini)
	{		
		
		dimension = d;
		fin = f;
		inicio = ini;
		barrier_ = barrera;
		rand_ = r;
		k_=k;
		seed = s;
		p_ = p;
		q_ = q;

		lattice_ = latt;
		Ph_ = ph;
		Ps_ = Ps;
		Pd_ = Pd;
		Pm_ = Pm;
		Pp_ = Pp;
		Pq_ = Pq;
		Np_ = Np;

	}

	public void run()
	{
		for(int x = inicio; x <= fin; ++x)
		{
			for(int y = 1; y < dimension; ++y)
			{
				seed[k_] = rand_.fishman_moore1(seed[k_]);
				double rr = rand_.fishman_moore1Normalizar(seed[k_]);
				if(rr<Ps_ && lattice_[x][y][p_] == 1 )
				{
					seed[k_] = rand_.fishman_moore1(seed[k_]);
					double rrp = rand_.fishman_moore1Normalizar(seed[k_]);
					seed[k_] = rand_.fishman_moore1(seed[k_]);
					double rrm = rand_.fishman_moore1Normalizar(seed[k_]);
					boolean prolifer = false,PhMayorIgualNp = false,rrpMenorrPp = false;
					seed[k_] = rand_.fishman_moore1(seed[k_]);
					double rr2 = rand_.fishman_moore1Normalizar(seed[k_]);
					
					if(rrp < Pp_)
					{

						rrpMenorrPp = true;
						Ph_[x][y][q_] = Ph_[x][y][p_] + 1;
						if(Ph_[x][y][q_] >= Np_)
						{
							PhMayorIgualNp = true;
							double divisor = (4-(lattice_[x+1][y][p_] + lattice_[x-1][y][p_] + lattice_[x][y+1][p_] + lattice_[x][y-1][p_]));
							double p1 = (1-lattice_[x-1][y][p_])/divisor;
							double p2 = (1-lattice_[x+1][y][p_])/divisor;
							double p3 = (1-lattice_[x][y-1][p_])/divisor;
							double p4 = (1-lattice_[x][y+1][p_])/divisor;
						
							
							if(rr2 <= p1)
							{
								lattice_[x-1][y][q_] = 1;
								lattice_[x][y][q_] = 1;
								Ph_[x-1][y][q_] = 0;
								prolifer = true;
							}else if(rr2 <= p1 + p2)
							{
								lattice_[x+1][y][q_] = 1;
								Ph_[x+1][y][q_] = 0;
								lattice_[x][y][q_] = 1;
								prolifer = true;
							}
							else if(rr2 <= p1 + p2 +p3)
							{
								lattice_[x][y-1][q_] = 1;
								Ph_[x][y-1][q_] = 0;
								lattice_[x][y][q_] = 1;
								prolifer = true;
							}
							else if(rr2 <= p1 + p2 + p3 + p4)
							{
								lattice_[x][y+1][q_] = 1;
								Ph_[x][y+1][q_] = 0;
								lattice_[x][y][q_] = 1;
								prolifer = true;
							}
						}	
					}

					if(!PhMayorIgualNp || !prolifer || !rrpMenorrPp)
					{
						if(rrm < Pm_  )
						{
							double divisor = (4-(lattice_[x+1][y][p_] + lattice_[x-1][y][p_] + lattice_[x][y+1][p_] + lattice_[x][y-1][p_]));
							double p1 = (1-lattice_[x-1][y][p_])/divisor;
							double p2 = (1-lattice_[x+1][y][p_])/divisor;
							double p3 = (1-lattice_[x][y-1][p_])/divisor;
							double p4 = (1-lattice_[x][y+1][p_])/divisor;
							if(rr2 <= p1)
							{
								lattice_[x-1%dimension][y][q_] = 1;
								lattice_[x][y][q_] = 0;
							}else if(rr2 <= p1 + p2)
							{
								lattice_[x+1%dimension][y][q_] = 1;
								lattice_[x][y][q_] = 0;
							}
							else if(rr2 <= p1 + p2 +p3)
							{
								lattice_[x][y-1%dimension][q_] = 1;
								lattice_[x][y][q_] = 0;
							}
							else if(rr2 <= p1 + p2 + p3 + p4) 
							{
								lattice_[x][y+1%dimension][q_] = 1;
								lattice_[x][y][q_] = 0;
							}
							else//este es el caso de que "intentáse" migrar pero todas las vecinas estuvieran ocupadas
							{
								lattice_[x][y][q_] = 1;
							}
							
						}
						else //Quiescent
						{
							lattice_[x][y][q_] = 1;	
						}
					}
				}	
				else
				{
					lattice_[x][y][q_] = 0;
				}
			}
		}
		try{
			barrier_.await();
		}catch(BrokenBarrierException e){ e.getMessage();}
		 catch(InterruptedException e){ e.getMessage(); }
	}
}